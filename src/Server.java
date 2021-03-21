import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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


            for(;;){
                Socket cl = s.accept();
                System.out.println("Cliente conectado desde "+cl.getInetAddress()+":"+cl.getPort());
                //crear Streams
                ObjectOutputStream oos = new ObjectOutputStream(cl.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(cl.getInputStream());
                mostrarArchivos(oos);

                //bucle de escucha de instrucciones
                int elec = (int) ois.readObject();
                switch (elec) {
                    case 1: {
                        //subirArchivo(cl);
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
                        System.out.println("Tenemos que eliminar algo");
                        break;
                    }
                    case 5: {
                        System.out.println("o esto otro");
                        break;
                    }
                }

                oos.close();
                ois.close();

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

    public void mostrarArchivos(ObjectOutputStream oos) throws IOException {
        File f = new File(dirActual);
        File []listaDeArchivos = f.listFiles();
        oos.writeObject(listaDeArchivos);
    }

    public static void main(String[] args){
        new Server();
    }//main
}
