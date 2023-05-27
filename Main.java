import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String args[]) {
        //varibles que definen los dias, pabellones y las semanas
        int numP = 2, numD = 7, semanas = 1;
        //Objeto compartido
        Registro tabla = new Registro(numP, numD);
        //Ciclo que crea los hilos
        for (int i = 0; i < numP; i++) {
            //Dos hilos por pabellon
            for (int j = 0; j < 2; j++) {
                Lector nuevo = new Lector(i, tabla, numD, semanas);
                nuevo.setName("p:"+i+" l:"+j);
                nuevo.start();
            }
        }
        System.out.println("\t\tEmpieza la simulacion\n\n");
    }
}

class Registro {
    //Matriz de registrtos
    private int[][] content;
    //Variables tipo bandera (son atomicas para
    // que los hilos las manipulen al gusto)
    public AtomicInteger cont0, cont1, contM;
    //Lista de pabellones
    List<String> pabellonesN = List.of("Jardin Botanico", "Pabellon Nacional");
    //Constructor
    public Registro(int numP, int numD) {
        this.content = new int[numP][numD];
        this.cont0 = new AtomicInteger(0);
        this.cont1 = new AtomicInteger(0);
        this.contM = new AtomicInteger(0);
    }
    //Metodo sincronizado que inserta los datos en la matriz
    synchronized public void inserta(int cantidad, int pabellon, int dia) {
        content[pabellon][dia] += cantidad;
    }
    //Metodo que imprime los datos al dia
    public void impresionD(int dia, int pabellon) {
        System.out.println("\tDia "+(dia+1)+"\nEl "+pabellonesN.get(pabellon)+" tuvo: "+this.content[pabellon][dia]);
    }
    //Metodo que imprime los datos a la semana
    public void impresionS() {
        System.out.println("\n\n\t\tReporte semanal");
        char[] a = {'L','M','M','J','V','S','D'};
        
        System.out.print(String.format("%20s|", ""));
        for (char c : a) {
            System.out.print(String.format("%-4s|", c));
        }
        System.out.println();
        for (int i = 0; i < pabellonesN.size(); i++) {
            System.out.print(String.format("%-20s|", pabellonesN.get(i)));
            for (int j = 0; j < this.content[0].length; j++) {
                System.out.print(String.format("%-4d|",this.content[i][j]));
            }
            System.out.println();
        }
        System.out.println("\n\n");
    }
    //Metodo que limpia la matriz en caso de haber mas de 1 semana
    public void limpiaM() {
        for (int i = 0; i < this.content.length; i++) {
            for (int j = 0; j < this.content[0].length; j++) {
                this.content[i][j] = 0;
            }
        }
    }
}

class Lector extends Thread{
    private int pabellon, dias, semanas;
    private Registro tabla;
    public Lector(int pabellon, Registro tabla, int dias, int semanas) {
        this.pabellon = pabellon;
        this.tabla = tabla;
        this.dias = dias;
        this.semanas = semanas;
    }
    //Metodo que define que hara cada hilo
    public void run() {
        //Ciclo para realizar semanas
        for (int h = 0; h < this.semanas; h++) {
            //Ciclo para realizar dias
            for (int i = 0; i < this.dias; i++) {
                //Ciclo para realizar horas
                for (int j = 0; j < 8; j++) {
                    int afluencia = 0;
                    //Generamos un valor para saber si registramos visitantes
                    int registro = (int)(Math.random()*100+1);
                    if (registro >= 1 && registro <= 70) {
                        //Generamos un numero de visitantes
                        afluencia = (int)(Math.random()*20+1);
                        this.tabla.inserta(afluencia, this.pabellon, i);
                    }else{
                        //Generamos un numero de visitantes igual a 0
                        this.tabla.inserta(0, this.pabellon, i);
                    }
                    //Esto imprime los visitantes por hora
                    // System.out.println(getName()+" obuvo: "+afluencia+" el dia:"+(i+1));
                    //Dormimos el hilo 1 seg
                    try {
                        sleep(1000);
                    } catch (Exception e) {
                        System.out.println("No durmio");
                    }
                }
                //Sincronizamos la impresion de los dias
                synchronized (this.tabla) {
                    //Controlamos el numero de impresiones por pabellon
                    if (pabellon == 0) {
                        //Solo un hilo puede imprimir, el que imprime es el ultimo lector
                        //que pasa por cada pabellon
                        if (this.tabla.cont0.get() != 0) {
                            this.tabla.cont0.set(0);
                            this.tabla.impresionD(i, this.pabellon);
                        } else {
                            this.tabla.cont0.getAndIncrement();
                        }
                    } else {
                        //Solo un hilo puede imprimir, el que imprime es el ultimo lector
                        //que pasa por cada pabellon
                        if (this.tabla.cont1.get() != 0) {
                            this.tabla.cont1.set(0);
                            this.tabla.impresionD(i, this.pabellon);
                        } else {
                            this.tabla.cont1.getAndIncrement();
                        }
                    }
                }
            }
            //Sincronizamos la impresion de cada semana
            synchronized (this.tabla) {
                this.tabla.contM.getAndIncrement();
                //Esperamos al ultimo hilo y volvemos a empezar
                if (this.tabla.contM.get() == 4) {
                    this.tabla.impresionS();
                    this.tabla.limpiaM();
                    this.tabla.contM.set(0);
                    this.tabla.notifyAll();
                }
            }
        }
    }
}