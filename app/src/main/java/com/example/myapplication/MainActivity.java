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

    public void onOperadorClick(View view) {
        Button boton = (Button) view;
        String operador = boton.getText().toString();
        String textoActual = pantalla.getText().toString();

        if (!textoActual.equals("Error")) {
            if (esNuevoNumero && isTrig(operador)) {
                pantalla.setText(operador);
                esNuevoNumero = false;
                return;
            }

            if (isTrig(operador) && !textoActual.isEmpty() && Character.isDigit(textoActual.charAt(textoActual.length() - 1))) {
                pantalla.append("*");
            }
            
            pantalla.append(operador);
            esNuevoNumero = false;
        }
    }

    public void onTrigClick(View view) {
        onOperadorClick(view);
    }

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

        double resultado = Double.parseDouble(tokens.get(0));

        for (int i = 1; i < tokens.size(); i += 2) {
            String op = tokens.get(i);
            double siguienteVal = Double.parseDouble(tokens.get(i + 1));

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
        if (Math.abs(res) < 1E-10) res = 0;
        return res;
    }

    private List<String> fragmentarExpresion(String expresion) {
        List<String> tokens = new ArrayList<>();
        String numeroAcumulado = "";

        for (int i = 0; i < expresion.length(); i++) {
            char c = expresion.charAt(i);

            if ("+-*/".indexOf(c) != -1) {
                if (!numeroAcumulado.isEmpty()) {
                    tokens.add(numeroAcumulado);
                    numeroAcumulado = "";
                }
                tokens.add(String.valueOf(c));
            } 
            else if (i + 2 < expresion.length() && isTrig(expresion.substring(i, i + 3))) {
                if (!numeroAcumulado.isEmpty()) {
                    tokens.add(numeroAcumulado);
                    numeroAcumulado = "";
                }
                String funcion = expresion.substring(i, i + 3);
                int j = i + 3;
                StringBuilder valTrig = new StringBuilder();

                if (j < expresion.length() && expresion.charAt(j) == '-') {
                    valTrig.append('-');
                    j++;
                }

                while (j < expresion.length() && (Character.isDigit(expresion.charAt(j)) || expresion.charAt(j) == '.')) {
                    valTrig.append(expresion.charAt(j));
                    j++;
                }

                String valorStr = valTrig.toString();
                double valorNum = 0;
                if (!valorStr.isEmpty() && !valorStr.equals("-")) {
                    valorNum = Double.parseDouble(valorStr);
                }

                double valorCalculado = aplicarTrig(funcion, valorNum);
                tokens.add(String.valueOf(valorCalculado));
                i = j - 1;
            } 
            else {
                numeroAcumulado += c;
            }
        }

        if (!numeroAcumulado.isEmpty()) tokens.add(numeroAcumulado);
        return tokens;
    }

    private String formatearResultado(double d) {
        if (d == (long) d) return String.format("%d", (long) d);
        else return String.valueOf(d);
    }

    public void onClearClick(View view) {
        pantalla.setText("0");
        esNuevoNumero = true;
    }

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