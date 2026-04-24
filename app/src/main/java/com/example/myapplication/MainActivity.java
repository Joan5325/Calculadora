package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView pantalla;
    private boolean esNuevoNumero = true;
    private boolean esRadianes = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pantalla = findViewById(R.id.pantalla);

        Switch swGrados = findViewById(R.id.switch1);
        Switch swRadianes = findViewById(R.id.switch2);

        if (swGrados != null && swRadianes != null) {
            // Inicialización: Grados por defecto
            swGrados.setChecked(true);
            esRadianes = false;

            swGrados.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    swRadianes.setChecked(false);
                    esRadianes = false;
                } else if (!swRadianes.isChecked()) {
                    swGrados.setChecked(true);
                }
            });

            swRadianes.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    swGrados.setChecked(false);
                    esRadianes = true;
                } else if (!swGrados.isChecked()) {
                    swRadianes.setChecked(true);
                }
            });
        }
    }

    // BOTONES 0-9
    public void onNumeroClick(View view) {
        Button boton = (Button) view;
        String numeroLeido = boton.getText().toString();

        if (esNuevoNumero || pantalla.getText().toString().equals("0") || pantalla.getText().toString().equals("Error")) {
            pantalla.setText(numeroLeido);
            esNuevoNumero = false;
        } else {
            pantalla.append(numeroLeido);
        }
    }

    // BOTONES +, -, *, /, SIN, COS, TAN
    public void onOperadorClick(View view) {
        Button boton = (Button) view;
        String operador = boton.getText().toString();
        String textoActual = pantalla.getText().toString();

        if (!textoActual.equals("Error") && !textoActual.isEmpty()) {
            if (esOperadorAlFinal(textoActual)) {
                pantalla.setText(reemplazarUltimoOperador(textoActual, operador));
            } else {
                pantalla.append(operador);
            }
            esNuevoNumero = false;
        }
    }

    public void onTrigClick(View view) {
        onOperadorClick(view);
    }

    private boolean esOperadorAlFinal(String texto) {
        if (texto.isEmpty()) return false;
        char ultimo = texto.charAt(texto.length() - 1);
        return ultimo == '+' || ultimo == '-' || ultimo == '*' || ultimo == '/' || 
               texto.endsWith("SIN") || texto.endsWith("COS") || texto.endsWith("TAN");
    }

    private String reemplazarUltimoOperador(String texto, String nuevoOp) {
        if (texto.endsWith("SIN") || texto.endsWith("COS") || texto.endsWith("TAN")) {
            return texto.substring(0, texto.length() - 3) + nuevoOp;
        }
        return texto.substring(0, texto.length() - 1) + nuevoOp;
    }

    // BOTÓN EXE (Resultado Final)
    public void onExeClick(View view) {
        String expresion = pantalla.getText().toString();
        if (expresion.isEmpty() || expresion.equals("Error")) return;

        try {
            double resultado = evaluarSinJerarquia(expresion);
            pantalla.setText(formatearResultado(resultado));
        } catch (Exception e) {
            pantalla.setText("Error");
        }
        esNuevoNumero = true;
    }

    private double evaluarSinJerarquia(String expresion) {
        List<String> tokens = fragmentarExpresion(expresion);
        if (tokens.isEmpty()) return 0;

        int i = 0;
        double resultado;

        // Primer valor
        if (isTrig(tokens.get(i))) {
            String trigOp = tokens.get(i);
            double val = Double.parseDouble(tokens.get(i + 1));
            resultado = aplicarTrig(trigOp, val);
            i += 2;
        } else {
            resultado = Double.parseDouble(tokens.get(i));
            i++;
        }

        while (i < tokens.size()) {
            String op = tokens.get(i);
            i++;

            if (i >= tokens.size()) break;

            double siguienteVal;
            if (isTrig(tokens.get(i))) {
                String trigOp = tokens.get(i);
                double val = Double.parseDouble(tokens.get(i + 1));
                siguienteVal = aplicarTrig(trigOp, val);
                i += 2;
            } else {
                siguienteVal = Double.parseDouble(tokens.get(i));
                i++;
            }

            switch (op) {
                case "+": resultado += siguienteVal; break;
                case "-": resultado -= siguienteVal; break;
                case "*": resultado *= siguienteVal; break;
                case "/":
                    if (siguienteVal != 0) resultado /= siguienteVal;
                    else throw new ArithmeticException();
                    break;
            }
        }
        return resultado;
    }

    private boolean isTrig(String token) {
        return token.equals("SIN") || token.equals("COS") || token.equals("TAN");
    }

    private double aplicarTrig(String op, double valor) {
        double radians = esRadianes ? valor : Math.toRadians(valor);
        double res = 0;
        switch (op) {
            case "SIN": res = Math.sin(radians); break;
            case "COS": res = Math.cos(radians); break;
            case "TAN": res = Math.tan(radians); break;
        }
        // Limpieza de precisión para valores como COS(90)
        if (Math.abs(res) < 1E-10) res = 0;
        return res;
    }

    private List<String> fragmentarExpresion(String expresion) {
        List<String> tokens = new ArrayList<>();
        StringBuilder temp = new StringBuilder();

        for (int i = 0; i < expresion.length(); i++) {
            char c = expresion.charAt(i);
            if (c == '+' || c == '-' || c == '*' || c == '/') {
                if (temp.length() > 0) {
                    tokens.add(temp.toString());
                    temp.setLength(0);
                }
                tokens.add(String.valueOf(c));
            } else if (Character.isLetter(c)) {
                if (temp.length() > 0 && !Character.isLetter(temp.charAt(0))) {
                    tokens.add(temp.toString());
                    temp.setLength(0);
                }
                temp.append(c);
                if (temp.length() == 3) {
                    tokens.add(temp.toString());
                    temp.setLength(0);
                }
            } else {
                temp.append(c);
            }
        }
        if (temp.length() > 0) tokens.add(temp.toString());
        return tokens;
    }

    private String formatearResultado(double d) {
        if (d == (long) d) return String.format("%d", (long) d);
        else return String.valueOf(d);
    }

    // BOTÓN LIMPIAR (AC)
    public void onClearClick(View view) {
        pantalla.setText("0");
        esNuevoNumero = true;
    }

    // BOTÓN BORRAR (DEL)
    public void onDeleteClick(View view) {
        String texto = pantalla.getText().toString();
        if (texto.length() > 1) {
            pantalla.setText(texto.substring(0, texto.length() - 1));
        } else {
            pantalla.setText("0");
            esNuevoNumero = true;
        }
    }
}