import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;

public class Server {
    private String dirActual;

    public Server(){
        try{
            ServerSocket s = new ServerSocket(8000);
            s.setReuseAddress(true);
            System.out.println("Servidor iniciado esperando por archivos...");
            File f = new File("");
            String ruta = f.getAbsolutePath();
            String carpeta="drive";
            String raiz = ruta+"\\"+carpeta+"\\";
            dirActual = raiz;
            System.out.println("ruta:"+raiz);
            File f2 = new File(raiz);
            f2.mkdirs();
            f2.setWritable(true);
            mostrarArchivos();

            for(;;){
                Socket cl = s.accept();
                System.out.println("Cliente conectado desde "+cl.getInetAddress()+":"+cl.getPort());
                DataInputStream dis = new DataInputStream(cl.getInputStream());
                String nombre = dis.readUTF();
                long tam = dis.readLong();
                System.out.println("Comienza descarga del archivo "+nombre+" de "+tam+" bytes\n\n");
                DataOutputStream dos = new DataOutputStream(new FileOutputStream(raiz+nombre));
                long recibidos=0;
                int l, porcentaje;
                while(recibidos<tam){
                    byte[] b = new byte[1500];
                    l = dis.read(b);
                    System.out.println("leidos: "+l);
                    dos.write(b,0,l);
                    dos.flush();
                    recibidos = recibidos + l;
                    porcentaje = (int)((recibidos*100)/tam);
                    System.out.print("\rRecibido el "+ porcentaje +" % del archivo");
                }//while
                System.out.println("Archivo recibido..");
                dos.close();
                dis.close();
                cl.close();
            }//for

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void mostrarArchivos(){
        File f = new File(dirActual);
        File []listaDeArchivos = f.listFiles();
        if (listaDeArchivos == null || listaDeArchivos.length == 0)
            System.out.println("No hay elementos dentro de la carpeta actual");
        else {
            System.out.print("Archivos en el servidor: \n");
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

    public static void main(String[] args){
        new Server();
    }//main
}
