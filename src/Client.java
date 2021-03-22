import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    private String dirActual = "drive";
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public Client(){
        try{
            Socket cl = new Socket("127.0.0.1",8000);
            System.out.println("Conexion con servidor establecida...");

            oos = new ObjectOutputStream(cl.getOutputStream());
            ois = new ObjectInputStream(cl.getInputStream());

            mostrarArchivos();

            int elec; //inicio de menu
            Scanner reader = new Scanner(System.in);
            do{
                System.out.println("\nMenu\n1-Subir un archivo o carpeta\n2-Descargar\n3-Eliminar un archivo o carpeta\n4-Cambiar directorio\n0-Salir\n\n>"); //ayuda
                elec= reader.nextInt();
                oos.writeObject(elec);
                switch (elec) {
                    case 1: {   //subir archivo o carpeta
                        System.out.println("Lanzando JFileChooser...");
                        subir();
                        mostrarArchivos();
                        break;
                    }
                    case 2: {   //descargar un archivo o carpeta
                        System.out.println("Descargar un archivo o carpeta");
                        descargar();
                        break;
                    }
                    case 3: {   //eliminar archivo o carpeta
                        System.out.println("Introduce el nombre del archivo pls: ");
                        break;
                    }
                    case 4: {   //cambiar directorio
                        cambiarDir();
                        break;
                    }
                }
            }while(elec!=0);
            oos.close();
            ois.close();
            cl.close();     //invocar hasta que queramos finalizar la conexión

        }catch(Exception e){
            e.printStackTrace();
        }//catch
    }

    public void mostrarArchivos() throws IOException, ClassNotFoundException {
        File []listaDeArchivos = (File[]) ois.readObject();
        if (listaDeArchivos == null || listaDeArchivos.length == 0)
            System.out.println("Directorio vacio");
        else {
            System.out.print("\n**** Archivos en "+dirActual+" ****"+"\n\n");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            for (File archivo : listaDeArchivos) {
                if(archivo.isDirectory())
                    System.out.printf("- (%s) %s  -- %s%n",
                            archivo.isDirectory() ? "dir" : "file",
                            archivo.getName(),
                            sdf.format(archivo.lastModified())
                    );
            }
            for (File archivo : listaDeArchivos) {
                if(!archivo.isDirectory())
                    System.out.printf("- (%s) %s -- %d kb -- %s%n",
                            archivo.isDirectory() ? "dir" : "file",
                            archivo.getName(),
                            archivo.length()/1024,
                            sdf.format(archivo.lastModified())
                    );
            }
        }
    }

    public void subir(){
        try {
            JFileChooser jf = new JFileChooser();
            //jf.setMultiSelectionEnabled(true);
            jf.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);  //para integrar en una sola opcion del menú el subir archivos y carpetas
            int r = jf.showOpenDialog(null);
            if(r==JFileChooser.APPROVE_OPTION){
                File f = jf.getSelectedFile();

                if(f.isDirectory()) subirDir(f);
                else subirArchivo(f);

            }
        }catch(Exception e){
            e.printStackTrace();
        }//catch
    }
    public void subirArchivo(File f) throws IOException {
        long tam = f.length();
        System.out.println("Enviando '"+f.getName()+"' de "+tam/1024+" kb");
        oos.writeObject(f);
        oos.flush();

        DataInputStream disf = new DataInputStream(new FileInputStream(f.getAbsolutePath()));
        long enviados = 0;
        int l;
        while (enviados<tam){
            byte[] b = new byte[1500];
            l=disf.read(b);
            oos.write(b, 0, l);
            oos.flush();
            enviados += l;
        }
        disf.close();
        System.out.println("Archivo enviado");
    }
    public void subirDir(File f) throws IOException {
        long tam = f.length();
        System.out.println("Preparandose para enviar archivo '"+f.getName()+"' de "+tam/1024+" kb");
        oos.writeObject(f);
        oos.flush();
    }

    public void descargar(){
        System.out.println("Ingresa el nombre del archivo: ");
    }

    public void cambiarDir() throws IOException, ClassNotFoundException {
        Scanner reader = new Scanner(System.in);

        String []listaDeDir = (String[]) ois.readObject(); //leer la lista de directorios
        System.out.println("Dirs: " + Arrays.toString(listaDeDir));
        System.out.println("Nombre del dir: ");
        if(!dirActual.equals("drive"))  //mostrar la opción de "atrás" para volver a la raíz (drive\)
            System.out.println("( .. para volver al inicio )");

        String elec = reader.nextLine();
        if(!Arrays.asList(listaDeDir).contains(elec) && !elec.equals("..")){  //comprobamos que el directorio solicitado existe
            elec = "";
            System.out.println("Dir invalido");
        }

        oos.writeObject(elec);
        oos.flush();
        if(elec.equals(".."))
            dirActual = "drive";
        else if(!elec.equals(""))
            dirActual = dirActual + "\\" + elec;
        mostrarArchivos();
    }

    public static void main(String[] args){
        new Client();
    }//main
}
