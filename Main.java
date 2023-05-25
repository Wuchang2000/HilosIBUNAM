public class Main {
    public static void main(String args[]) {
        int numP = 2, numD = 7;
        Registro tabla = new Registro(numP, numD);
        Temporizador reloj = new Temporizador(tabla, numD);
        reloj.start();
        for (int i = 0; i < numP; i++) {
            for (int j = 0; j < 2; j++) {
                Lector nuevo = new Lector(i, tabla, reloj);
                nuevo.start();
            }
        }
    }
}

class Temporizador extends Thread {
    private int dias;
    private Registro tabla;
    public Temporizador(Registro tabla, int dias) {
        this.dias = dias;
        this.tabla = tabla;
    }
    public void run() {
        for (int j = 0; j < dias; j++) {
            for (int i = 0; i < 8; i++) {
                try {
                    sleep(200);
                } catch (Exception e) {
                    System.out.println("No durmio");
                }
                synchronized (this){
                    notifyAll();
                }
            }
            synchronized (this){
                if (j < 6) {
                    tabla.incremento();
                }
                impresionD(j);
                notifyAll();
            }
        }
        impresionS();
    }

    public void impresionD(int dia) {
        System.out.println("\tDia "+(dia+1));
        for (int i = 0; i < this.tabla.content.length; i++) {
            System.out.println("El pabellon: "+i+" tuvo: "+this.tabla.content[i][dia]);
        }
    }

    public void impresionS() {
        System.out.println("Reporte semanal");
        char[] a = {'L','M','M','J','V','S','D'};
        System.out.print("\t\t|");
        for (char c : a) {
            System.out.print(String.format("%-4s|", c));
        }
        System.out.println();
        for (int i = 0; i < this.tabla.content.length; i++) {
            System.out.print("Pabellon:"+(i+1)+"\t|");
            for (int j = 0; j < this.tabla.content[0].length; j++) {
                System.out.print(String.format("%-4d|",this.tabla.content[i][j]));
            }
            System.out.println();
        }
    }
}

class Registro {
    public int[][] content;
    private int dia;
    public Registro(int numP, int numD) {
        this.content = new int[numP][numD];
        this.dia = 0;
    }
    synchronized public void inserta(int cantidad, int pabellon) {
        content[pabellon][this.dia] += cantidad;
    }
    public void incremento() {
        this.dia++;
    }
    public int getdia() {
        return dia;
    }
}

class Lector extends Thread{
    private int pabellon;
    private Registro tabla;
    private Temporizador reloj;
    public Lector(int pabellon, Registro tabla, Temporizador reloj) {
        this.pabellon = pabellon;
        this.tabla = tabla;
        this.reloj = reloj;
    }
    public void run() {
        int afluencia = 0;
        int registro = (int)(Math.random()*100+1);
        if (registro >= 1 && registro <= 70) {
            afluencia = (int)(Math.random()*20+1);
            this.tabla.inserta(afluencia, this.pabellon);
        }else{
            this.tabla.inserta(0, this.pabellon);
        }
        // System.out.println(getName()+" obuvo: "+afluencia+" el dia:"+this.tabla.getdia());
        synchronized (reloj){
            try {
                reloj.wait();
                if (reloj.isAlive()) {
                    this.run();
                }
            } catch (Exception e) {
                System.out.println("Nop");
            }
        }
    }
}