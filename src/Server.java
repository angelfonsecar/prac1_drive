import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private String dirActual;
    private String raiz;
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
            raiz = ruta+"\\"+carpeta+"\\";
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
                        case 1: {   //subir archivo o carpeta
                            subir();
                            mostrarArchivos();
                            break;
                        }
                        case 2: {
                            System.out.println("El cliente quiere descargar");
                            break;
                        }
                        case 3: {
                            eliminar();
                            mostrarArchivos();
                            break;
                        }
                        case 4: {
                            System.out.println("cambiar dir");
                            cambiarDir();
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
        if(f.isDirectory()) {
            System.out.println("\nEl cliente quiere subir carpeta");
            subirDir(f);
        }
        else {
            System.out.println("\nEl cliente quiere subir un archivo");
            subirArchivo(f);
        }
    }
    public void subirArchivo(File f) throws IOException {
        long tam = f.length();

        System.out.println("Comienza descarga del archivo '"+f.getName()+"' de "+tam/1024+" kb");
        System.out.println("Subiendo a "+dirActual);
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
        System.out.println("carpeta a subir:"+f.getName());
    }

    public void eliminar() throws IOException, ClassNotFoundException {     //trabajo en este
        String elec = (String) ois.readObject();
        File fileToDelete = new File(dirActual+"\\"+elec);
        if(fileToDelete.exists()){
            System.out.println("eliminando "+fileToDelete.getAbsolutePath());
            if (!fileToDelete.isDirectory()) {  //eliminar archivo
                if(fileToDelete.delete())
                    oos.writeObject("Archivo eliminado");
                else
                    oos.writeObject("Error al eliminar archivo");
            }
            else {        //eliminar directorio
                if(deleteDirectory(fileToDelete))
                    oos.writeObject("Dir eliminado");
                else
                    oos.writeObject("Error al eliminar dir");
            }
        }else oos.writeObject("El archivo o dir no existe");
    }
    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents)
                deleteDirectory(file);
        }
        return directoryToBeDeleted.delete();
    }

    public void cambiarDir() throws IOException, ClassNotFoundException {   //estoy trabajando en este
        String elec = (String) ois.readObject();
        File f = new File(dirActual+"\\"+elec);

        boolean existeDir = f.isDirectory() || elec.equals("..");
        if(!existeDir){  //comprobamos que el directorio solicitado existe
            elec = "";
            System.out.println("Dir invalido");
        }
        oos.writeObject(existeDir);
        System.out.println("Entrar a '"+elec+"'");
        if(elec.equals("..")){
            dirActual = raiz;
        }else if(!elec.equals("")){
            dirActual = dirActual + elec + "\\";
        }
        mostrarArchivos();
    }

    public static void main(String[] args){
        new Server();
    }//main
}
