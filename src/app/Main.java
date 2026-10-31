package app;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opción: ", 1, 5);
            switch (opcion) {
                case 1 -> registrarEntrada();
                case 2 -> registrarSalida();
                case 3 -> reporteOcupacion();
                case 4 -> reporteFinanciero();
                default -> imprimir("Hasta pronto.");
            }
        } while (opcion != 5);
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
        imprimir("2. Registrar salida");
        imprimir("3. Reporte de ocupación");
        imprimir("4. Reporte financiero");
        imprimir("5. Salir");
    }

    static void imprimir(String mensaje) {2
        System.out.println(mensaje);
    }

    static void registrarEntrada() {
        int celda = celdaDisponible();
        if (celda == -1) {
            imprimir("El parqueadero está lleno. No es posible registrar el ingreso.");
            return;
        }
        String tipo = leerTipoVehiculo();
        LocalDateTime ingreso = leerFechaHora("Fecha y hora de ingreso (yyyy-MM-dd HH:mm): ");

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

    static void registrarSalida() {
        if (celdasOcupadas == 0) {
            imprimir("No hay vehículos en el parqueadero.");
            return;
        }
        int celda = leerEntero("Número de celda (1-" + CAPACIDAD_TOTAL + "): ", 1, CAPACIDAD_TOTAL) - 1;
        if (tiposVehiculo[celda] == null) {
            imprimir("La celda " + (celda + 1) + " está libre.");
            return;
        }

        LocalDateTime ingreso = fechasIngreso[celda];
        LocalDateTime salida;
        while (true) {
            salida = leerFechaHora("Fecha y hora de salida (yyyy-MM-dd HH:mm): ");
            if (salida.isBefore(ingreso)) {
                imprimir("La salida no puede ser anterior al ingreso (" + ingreso.format(FORMATO_FECHA_HORA) + ").");
                continue;
            }
            break;
        }

        String tipo = tiposVehiculo[celda];
        long total = liquidar(celda, tipo, ingreso, salida);

        if (tipo.equals(CARRO)) {
            recaudoCarros += total;
        } else {
            recaudoMotos += total;
        }
        tiposVehiculo[celda] = null;
        fechasIngreso[celda] = null;
        celdasOcupadas--;
        celdasLibres++;
    }

    static long liquidar(int celda, String tipo, LocalDateTime ingreso, LocalDateTime salida) {
        long diasACobrar = contarDiasCompletos(ingreso, salida);
        LocalDateTime cursor = ingreso.plusDays(diasACobrar);
        long horasACobrar = calcularHorasRestantes(cursor, salida);
        if (horasACobrar >= UMBRAL_HORAS_DIA) {
            diasACobrar++;
            horasACobrar = 0;
        }

        long tarifaDia = tipo.equals(CARRO) ? TARIFA_DIA_CARRO : TARIFA_DIA_MOTO;
        long tarifaHora = tipo.equals(CARRO) ? TARIFA_HORA_CARRO : TARIFA_HORA_MOTO;
        long subtotalDias = diasACobrar * tarifaDia;
        long subtotalHoras = horasACobrar * tarifaHora;
        long total = subtotalDias + subtotalHoras;

        mostrarFactura(celda, tipo, ingreso, salida, diasACobrar, tarifaDia, horasACobrar, tarifaHora, total);
        return total;
    }

    static long contarDiasCompletos(LocalDateTime ingreso, LocalDateTime salida) {
        return ChronoUnit.DAYS.between(ingreso, salida);
    }

    static long calcularHorasRestantes(LocalDateTime cursor, LocalDateTime salida) {
        long minutosRestantes = Duration.between(cursor, salida).toMinutes();
        return (minutosRestantes + 59) / 60;
    }

    static void mostrarFactura(int celda, String tipo, LocalDateTime ingreso, LocalDateTime salida,
                               long dias, long tarifaDia, long horas, long tarifaHora, long total) {
        imprimir("\n------------- FACTURA -------------");
        imprimir("Celda liberada : " + (celda + 1));
        imprimir("Tipo           : " + tipo);
        imprimir("Ingreso        : " + ingreso.format(FORMATO_FECHA_HORA));
        imprimir("Salida         : " + salida.format(FORMATO_FECHA_HORA));
        if (dias > 0) {
            imprimir("Días           : " + dias + " x " + pesos(tarifaDia) + " = " + pesos(dias * tarifaDia));
        }
        if (horas > 0) {
            imprimir("Horas          : " + horas + " x " + pesos(tarifaHora) + " = " + pesos(horas * tarifaHora));
        }
        imprimir("TOTAL A PAGAR  : " + pesos(total));
        imprimir("-----------------------------------");
    }

    static String pesos(long valor) {
        String digitos = Long.toString(valor);
        StringBuilder sb = new StringBuilder();
        int contador = 0;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            if (contador > 0 && contador % 3 == 0) {
                sb.insert(0, '.');
            }
            sb.insert(0, digitos.charAt(i));
            contador++;
        }
        return "$" + sb;
    }

    static void reporteOcupacion() {
        imprimir("\n--- Reporte de ocupación ---");
        imprimir("Celdas ocupadas: " + celdasOcupadas);
        imprimir("Celdas libres  : " + celdasLibres);
    }

    static void reporteFinanciero() {
        imprimir("\n--- Reporte financiero ---");
        imprimir("Recaudo carros : " + pesos(recaudoCarros));
        imprimir("Recaudo motos  : " + pesos(recaudoMotos));
        imprimir("Total general  : " + pesos(recaudoCarros + recaudoMotos));
    }

    static int leerEntero(String mensaje, int min, int max) {
        while (true) {
            System.out.print(mensaje);
            String texto = SCANNER.nextLine().trim();
            try {
                int valor = Integer.parseInt(texto);
                if (valor >= min && valor <= max) {
                    return valor;
                }
            } catch (NumberFormatException e) {
            }
            imprimir("Dato inválido. Ingrese un número entre " + min + " y " + max + ".");
        }
    }

    static String leerTipoVehiculo() {
        int opcion = leerEntero("Tipo de vehículo (1. Carro, 2. Moto): ", 1, 2);
        return opcion == 1 ? CARRO : MOTO;
    }

    static LocalDateTime leerFechaHora(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = SCANNER.nextLine().trim();
            if (!formatoFechaHoraValido(texto)) {
                imprimir("Fecha u hora inválidas. Use yyyy-MM-dd HH:mm y el horario 08:00-22:00.");
                continue;
            }
            LocalDateTime fecha = LocalDateTime.parse(texto, FORMATO_FECHA_HORA);
            if (!fecha.format(FORMATO_FECHA_HORA).equals(texto)) {
                imprimir("La fecha no existe en el calendario.");
                continue;
            }
            return fecha;
        }
    }

    static boolean formatoFechaHoraValido(String texto) {
        if (texto.length() != 16
                || texto.charAt(4) != '-'
                || texto.charAt(7) != '-'
                || texto.charAt(10) != ' '
                || texto.charAt(13) != ':') {
            return false;
        }

        int anio = leerDigitos(texto, 0, 4);
        int mes = leerDigitos(texto, 5, 2);
        int dia = leerDigitos(texto, 8, 2);
        int hora = leerDigitos(texto, 11, 2);
        int minuto = leerDigitos(texto, 14, 2);

        return anio >= 1 && mes >= 1 && mes <= 12 && dia >= 1 && dia <= 31
                && hora >= HORA_APERTURA && hora <= HORA_CIERRE
                && (hora != HORA_CIERRE || minuto <= 0)
                && minuto >= 0 && minuto <= 59;
    }

    static int leerDigitos(String texto, int inicio, int cantidad) {
        int valor = 0;
        for (int i = inicio; i < inicio + cantidad; i++) {
            char caracter = texto.charAt(i);
            if (caracter < '0' || caracter > '9') {
                return -1;
            }
            valor = valor * 10 + caracter - '0';
        }
        return valor;
    }

}
