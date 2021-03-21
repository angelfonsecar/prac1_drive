import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
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
                System.out.println("\nMenu\n1-Subir un archivo\n2-Subir una carpeta\n3-Descargar\n4-Eliminar un archivo o carpeta\n5-Cambiar directorio\n0-Salir\n\n>"); //ayuda
                elec= reader.nextInt();
                oos.writeObject(elec);
                switch (elec) {
                    case 1: {   //subir archivo
                        System.out.println("Lanzando JFileChooser...");
                        subirArchivo();
                        mostrarArchivos();
                        break;
                    }
                    case 2: {   //subir carpeta
                        System.out.println("Subir carpeta");
                        break;
                    }
                    case 3: {   //descargar un archivo o carpeta
                        System.out.println("Descargar un archivo o carpeta");
                        descargar();
                        break;
                    }
                    case 4: {   //eliminar archivo o carpeta
                        System.out.println("Introduce el nombre del archivo pls: ");
                        break;
                    }
                    case 5: {   //cambiar directorio
                        System.out.println("o esto otro");
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
            System.out.println("No hay elementos dentro de la carpeta actual");
        else {
            System.out.print("\n****Archivos en "+dirActual+"\n\n");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            for (File archivo : listaDeArchivos) {
                System.out.printf("- %s (%s) -- %d KB -- %s%n",
                        archivo.getName(),
                        archivo.isDirectory() ? "dir" : "file",
                        archivo.length()/1024,
                        sdf.format(archivo.lastModified())
                );
            }
        }
    }

    public void subirArchivo(){
        try {
            JFileChooser jf = new JFileChooser();
            //jf.setMultiSelectionEnabled(true);
            int r = jf.showOpenDialog(null);
            if(r==JFileChooser.APPROVE_OPTION){
                File f = jf.getSelectedFile();
                String nombre = f.getName();
                String path = f.getAbsolutePath();
                long tam = f.length();
                System.out.println("Preparandose pare enviar archivo '"+nombre+"' de "+tam/1024+" kb");
                oos.writeObject(f);
                oos.flush();

                DataInputStream disf = new DataInputStream(new FileInputStream(path));
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
            }//if
        }catch(Exception e){
            e.printStackTrace();
        }//catch
    }

    public void descargar(){
        System.out.println("Ingresa el nombre del archivo: ");
    }

    public void cambiarDir(){
        if(!dirActual.equals("drive")){
            //mostrar la opción de "atrás" o "volver a la raíz" (drive\)
        }else {

        }
    }

    public static void main(String[] args){
        new Client();
    }//main
}
