import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class Client {

    public Client(){
        try{
            Socket cl = new Socket("127.0.0.1",8000);
            System.out.println("Conexion con servidor establecida...");

            ObjectOutputStream oos = new ObjectOutputStream(cl.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(cl.getInputStream());

            File []listaDeArchivos = (File[]) ois.readObject();
            mostrarArchivos(listaDeArchivos);

            int elec; //inicio de menu
            Scanner reader = new Scanner(System.in);
            do{
                System.out.println("\nMenu\n1-Subir un archivo\n2-Subir una carpeta\n3-Descargar\n4-Eliminar un archivo\n5-Eliminar una carpeta\n6-Cambiar directorio\n0-Salir\n\n>"); //ayuda
                elec= reader.nextInt();

                oos.writeObject(elec);
                switch (elec) {
                    case 1: {
                        subirArchivo(cl);
                        System.out.println("Lanzando FileChooser..");
                        break;
                    }
                    case 2: {
                        System.out.println("Aun no programo esto");
                        break;
                    }
                    case 3: {
                        System.out.println("Ni esto");
                        break;
                    }
                    case 4: {
                        //eliminar archivo
                        System.out.println("Introduce el nombre del archivo pls: ");
                        break;
                    }
                    case 5: {
                        System.out.println("o esto otro");
                        break;
                    }
                }
             }while(elec!=0);

            cl.close();     //invocar hasta que queramos finalizar la conexión

        }catch(Exception e){
            e.printStackTrace();
        }//catch
    }

    public void mostrarArchivos(File []listaDeArchivos){
        if (listaDeArchivos == null || listaDeArchivos.length == 0)
            System.out.println("No hay elementos dentro de la carpeta actual");
        else {
            System.out.print("\nArchivos en el drive: \n");
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

    public void subirArchivo(Socket cl){
        try {
            JFileChooser jf = new JFileChooser();
            //jf.setMultiSelectionEnabled(true);
            int r = jf.showOpenDialog(null);
            if(r==JFileChooser.APPROVE_OPTION){
                File f = jf.getSelectedFile();
                String nombre = f.getName();
                String path = f.getAbsolutePath();
                long tam = f.length();
                System.out.println("Preparandose pare enviar archivo "+path+" de "+tam+" bytes\n\n");
                DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
                DataInputStream dis = new DataInputStream(new FileInputStream(path));
                dos.writeUTF(nombre);
                dos.flush();
                dos.writeLong(tam);
                dos.flush();
                long enviados = 0;
                int l,porcentaje;
                while(enviados<tam){
                    byte[] b = new byte[1500];
                    l=dis.read(b);
                    System.out.println("enviados: "+l);
                    dos.write(b,0,l);
                    dos.flush();
                    enviados = enviados + l;
                    porcentaje = (int)((enviados*100)/tam);
                    System.out.print("\rEnviado el "+porcentaje+" % del archivo");
                }//while
                System.out.println("\nArchivo enviado..");
                dis.close();
                dos.close();

            }//if
        }catch(Exception e){
            e.printStackTrace();
        }//catch

    }

    public static void main(String[] args){
        new Client();
    }//main
}
