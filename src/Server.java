import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private String dirActual;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

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

                oos = new ObjectOutputStream(cl.getOutputStream());
                ois = new ObjectInputStream(cl.getInputStream());
                mostrarArchivos();

                while(true){//bucle de escucha de instrucciones
                    int elec = (int) ois.readObject();
                    if(elec==0) break;
                    switch (elec) {
                        case 1: {
                            System.out.println("\nEl cliente quiere subir un archivo");
                            subir();
                            mostrarArchivos();
                            break;
                        }
                        case 2: {
                            System.out.println("El cliente quiere subir una carpeta");
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
                }
                oos.close();
                ois.close();
                cl.close();
            }//for

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void mostrarArchivos() throws IOException {
        File f = new File(dirActual);
        File []listaDeArchivos = f.listFiles();
        oos.writeObject(listaDeArchivos);
    }

    public void subir() throws IOException, ClassNotFoundException {
        File f = (File)ois.readObject();
        if(f.isDirectory()) subirDir(f);
        else subirArchivo(f);
    }

    public void subirArchivo(File f) throws IOException {
        long tam = f.length();

        System.out.println("Comienza descarga del archivo '"+f.getName()+"' de "+tam/1024+" kb");
        DataOutputStream dosf = new DataOutputStream(new FileOutputStream(dirActual+f.getName()));

        long recibidos=0;
        int l;
        while(recibidos<tam){
            byte[] b = new byte[1500];
            l = ois.read(b,0,b.length);
            dosf.write(b,0,l);
            dosf.flush();
            recibidos += l;
        }//while

        System.out.println("Archivo recibido");
        dosf.close();
    }

    public void subirDir(File f){

    }

    public static void main(String[] args){
        new Server();
    }//main
}
