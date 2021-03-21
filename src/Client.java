import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;

public class Client {

    public Client(){
        try{
            Socket cl = new Socket("127.0.0.1",8000);
            System.out.println("Conexion con servidor establecida...");

            ObjectInputStream oos = new ObjectInputStream(cl.getInputStream());
            File []listaDeArchivos = (File[]) oos.readObject();
            mostrarArchivos(listaDeArchivos);

            System.out.println("Lanzando FileChooser..");
            //subirArchivo(cl);
            cl.close();     //invocar hasta que queramos finalizar la conexión

        }catch(Exception e){
            e.printStackTrace();
        }//catch
    }

    public void mostrarArchivos(File []listaDeArchivos){
        if (listaDeArchivos == null || listaDeArchivos.length == 0)
            System.out.println("No hay elementos dentro de la carpeta actual");
        else {
            System.out.print("Archivos en el drive: \n");
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
