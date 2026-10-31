package app;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {

    static final int CAPACIDAD_TOTAL = 10;
    static final long TARIFA_HORA_CARRO = 4000;
    static final long TARIFA_DIA_CARRO = 30000;
    static final long TARIFA_HORA_MOTO = 2000;
    static final long TARIFA_DIA_MOTO = 15000;
    static final int UMBRAL_HORAS_DIA = 8;
    static final int HORA_APERTURA = 8;
    static final int HORA_CIERRE = 22;

    static final String CARRO = "Carro";
    static final String MOTO = "Moto";

    static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    static final Scanner SCANNER = new Scanner(System.in);

    static String[] tiposVehiculo = new String[CAPACIDAD_TOTAL];
    static LocalDateTime[] fechasIngreso = new LocalDateTime[CAPACIDAD_TOTAL];

    static int celdasOcupadas;
    static int celdasLibres;
    static long recaudoCarros;
    static long recaudoMotos;

    public static void main(String[] args) {
        inicializarArreglos();
        imprimir("Bienvenido al sistema parqueadero");
        String opcion;
        do {
            mostrarMenu();
            System.out.print("Seleccione una opción: ");
            opcion = SCANNER.nextLine().trim();
            switch (opcion) {
                case "1" -> registrarEntrada();
                case "2" -> imprimir("Hasta pronto.");
                default -> imprimir("Opción no válida.");
            }
        } while (!opcion.equals("2"));
        SCANNER.close();
    }

    static void inicializarArreglos() {
        for (int i = 0; i < CAPACIDAD_TOTAL; i++) {
            tiposVehiculo[i] = null;
            fechasIngreso[i] = null;
        }
        celdasOcupadas = 0;
        celdasLibres = CAPACIDAD_TOTAL;
        recaudoCarros = 0;
        recaudoMotos = 0;
    }

    static void mostrarMenu() {
        imprimir("\n===== PARQUEADERO =====");
        imprimir("1. Registrar entrada");
        imprimir("2. Salir");
    }

    static void imprimir(String mensaje) {
        System.out.println(mensaje);
    }

    static void registrarEntrada() {
        int celda = celdaDisponible();
        if (celda == -1) {
            imprimir("El parqueadero está lleno. No es posible registrar el ingreso.");
            return;
        }

        System.out.print("Tipo de vehículo (1. Carro, 2. Moto): ");
        String opcionTipo = SCANNER.nextLine().trim();
        if (!opcionTipo.equals("1") && !opcionTipo.equals("2")) {
            imprimir("Tipo de vehículo no válido. Se cancela el registro.");
            return;
        }
        String tipo = opcionTipo.equals("1") ? CARRO : MOTO;

        System.out.print("Fecha y hora de ingreso (yyyy-MM-dd HH:mm): ");
        LocalDateTime ingreso;
        try {
            ingreso = LocalDateTime.parse(SCANNER.nextLine().trim(), FORMATO_FECHA_HORA);
        } catch (RuntimeException e) {
            imprimir("Fecha u hora inválidas. Se cancela el registro.");
            return;
        }

        tiposVehiculo[celda] = tipo;
        fechasIngreso[celda] = ingreso;
        celdasOcupadas++;
        celdasLibres--;
        imprimir(tipo + " registrado en la celda " + (celda + 1)
                + " a las " + ingreso.format(FORMATO_FECHA_HORA) + ".");
    }

    static int celdaDisponible() {
        for (int i = 0; i < CAPACIDAD_TOTAL; i++) {
            if (tiposVehiculo[i] == null) {
                return i;
            }
        }
        return -1;
    }

}
