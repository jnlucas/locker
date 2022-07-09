import java.net.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList; 


public class BikeLock implements Runnable{
  
    StringBuilder responseString = new StringBuilder();

    PrintWriter writer = null;
    BufferedReader bufferedReader = null;	

    public Socket cliente;

    public BikeLock(Socket cliente){
        this.cliente = cliente;
    }


	
    public static void main(String[] args) {
 
        StringBuilder responseString = new StringBuilder();

        PrintWriter writer = null;
        BufferedReader bufferedReader = null;

        
	    try {

            ServerSocket servidor = new ServerSocket(2222,50,InetAddress.getByName("10.0.0.4"));
            System.out.println("Servidor ouvindo a porta 2222");

            long TICKS_AT_EPOCH = 621355968000000000L;
            long tick = System.currentTimeMillis()*10000 + TICKS_AT_EPOCH;


            Comando comando = new Comando();
      
            String emei = "";
      
            while(true) {
        
                Socket cliente = servidor.accept();


                BikeLock tratamento = new BikeLock(cliente);
                Thread t = new Thread(tratamento);
                // Inicia a thread para o cliente conectado
                t.start();
            }

        }
        catch(Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }


    public void run(){

            Comando comando = new Comando();
      
            String emei = "";

        try{

                emei = comando.getEmei(this.cliente);

                String trava = comando.verificaTrava(emei);


                String cmd_ReL1  = "*CMDS,AL,"+emei+",20212411012200,Re,L1#\n";

                String cmd_S5 ="*CMDS,AL,"+emei+",20212411012200,S5#\n";

                String cmd_D0 = "*CMDS,AL,"+emei+",20212411012200,D0#\n";
	
	            String cmd_ReL0 ="*CMDS,AL,"+emei+",20212411012200,Re,L0#\n";
                
                String cmd_ReD0 ="*CMDS,AL,"+emei+",20212411012200,Re,D0#\n";
                
                
                String cmd_travar ="*CMDS,OM,"+emei+",20212411012200,L0,0,1,1621906458#\n";

                String cmd_travar_re ="*CMDS,OM,"+emei+",20212411012200,Re,L0#\n";
                
	            
       	        DataOutputStream  dout = new DataOutputStream(this.cliente.getOutputStream());

                
                dout.write(comando.getSendOrder(cmd_ReL1));
                dout.flush();

                System.out.print("\n enviando S5 ");

                dout.write(comando.getSendOrder(cmd_S5));
                dout.flush();

                InputStream is2 = this.cliente.getInputStream();
                byte[] buf = new byte[1024];
                
                int read;
                int cont = 0;

                System.out.print("\n S5 enviado ");

                while((read = is2.read(buf)) != -1 ) {
                    String output = new String(buf, 0, read);
                    comando.enviarServidorBike(output);
                    System.out.print(output);
                    System.out.flush();

                    if( output.contains("D0")){
                        System.out.print("\n fechando D0");
                        dout.write(comando.getSendOrder(cmd_ReD0));
                        dout.flush();
                    }

                    if( output.contains("L0")){
                        System.out.print("\n fechando L0");
                        dout.write(comando.getSendOrder(cmd_ReL0));
                        dout.flush();
                    }
                    if( output.contains("L1")){
                        System.out.print("\n fechando L1");
                        dout.write(comando.getSendOrder(cmd_ReL1));
                        dout.flush();
                    }
                    // cliente.close();
                    // dout.close();
                    break; 
                
                }
                

                // enviando comando de trava

                if(trava != ""){

                    System.out.print("\n enviando L0 para travar ");

                    dout.write(comando.getSendOrder(cmd_travar));
                    dout.flush();

                    System.out.print("\n  L0 enviado para travar");


                    while((read = is2.read(buf)) != -1 ) {
                        String output = new String(buf, 0, read);
                        //comando.enviarServidorBike(output);
                        System.out.print(output);
                        System.out.flush();

                        if( output.contains("D0")){
                            System.out.print("\n fechando D0");
                            dout.write(comando.getSendOrder(cmd_ReD0));
                            dout.flush();
                        }

                        if( output.contains("L0")){
                            System.out.print("\n fechando L0");
                            dout.write(comando.getSendOrder(cmd_travar_re));
                            dout.flush();
                        }
                        if( output.contains("L1")){
                            System.out.print("\n fechando L1");
                            dout.write(comando.getSendOrder(cmd_ReL1));
                            dout.flush();
                        }
                        this.cliente.close();
                        dout.close();
                        break; 
                    }

                }

                // fim comando de trava





                System.out.print("\n enviando D0 ");

                dout.write(comando.getSendOrder(cmd_D0));
                dout.flush();

                System.out.print("\n  D0 enviado ");


                while((read = is2.read(buf)) != -1 ) {
                    String output = new String(buf, 0, read);
                    comando.enviarServidorBike(output);
                    System.out.print(output);
                    System.out.flush();

                    if( output.contains("D0")){
                        System.out.print("\n fechando D0");
                        dout.write(comando.getSendOrder(cmd_ReD0));
                        dout.flush();
                    }

                    if( output.contains("L0")){
                        System.out.print("\n fechando L0");
                        dout.write(comando.getSendOrder(cmd_ReL0));
                        dout.flush();
                    }
                    if( output.contains("L1")){
                        System.out.print("\n fechando L1");
                        dout.write(comando.getSendOrder(cmd_ReL1));
                        dout.flush();
                    }
                    this.cliente.close();
                    dout.close();
                    break; 
                }


	            System.out.print("\n saindo do loop");
	            this.cliente.close();
                dout.close();


        }catch(Exception e){

        }



    }
}

class Comando {

    public byte[] getSendOrder(String comando){


	    byte[] order = comando.getBytes();
	    // add 0xFF,0xFF
	    return addByte(new byte[]{(byte) 0xFF,(byte) 0xFF},order);
    }

    public String verificaTrava(String emei){
        
        String destravar = "";
        String connectionUrl =
                "jdbc:sqlserver://bikego_prd.sqlserver.dbaas.com.br:1433;"
                        + "database=bikego_prd;"
                        + "user=bikego_prd;"
                        + "password=bikegoprd;"
                        + "encrypt=true;"
                        + "trustServerCertificate=true;"
                        + "loginTimeout=30;";

        try (Connection conn = DriverManager.getConnection(connectionUrl);) {
           System.out.println("conectado no banco de dados");

           String sql = "SELECT * FROM USER_BIKE_TRAVA UBT INNER JOIN BIKE B ON UBT.bike_id = B.id WHERE UBT.is_destravado IS NULL OR UBT.is_destravado  = 0 and B.imei = '"+emei+"'";

           Statement stmt = conn.createStatement();
           ResultSet rs;
 
            rs = stmt.executeQuery(sql);
            while ( rs.next() ) {

                String teste = rs.getString("imei");
                destravar = teste;
                System.out.println(teste);
            }
            conn.close();

            return destravar;
        }
        // Handle any errors that may have occurred.
        catch (SQLException e) {
            e.printStackTrace();
        }
        return destravar;
    }

    public String getEmei(Socket cli){
	
	    String emei = "";
        try{
            System.out.println("\n Cliente conectado: " + cli.getInetAddress().getHostAddress());

            InputStream is = cli.getInputStream();
            byte[] buffer = new byte[1024];
            int read;
            while((read = is.read(buffer)) != -1) {
            String output = new String(buffer, 0, read);
            emei = output.substring(9,24);
            System.out.print("cliente: "+emei+" \n");
            System.out.flush();
            break;
        }
        }catch(Exception e){
            return null;
        }
	    return emei;
    }



    public void enviarServidorBike(String mensagem){
        try{
 
           URL url = new URL("https://portalbikego.azurewebsites.net/api/travas/"+mensagem);
           HttpURLConnection http = (HttpURLConnection)url.openConnection();
           http.setRequestProperty("Accept", "application/json");

           System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
           http.disconnect();

        }catch(Exception e){
	
	    }
    }


    public byte[] addByte(byte[] b1,byte[] b2){
        byte[] b = new byte[b1.length+b2.length];
        System.arraycopy(b1, 0, b, 0, b1.length);
        System.arraycopy(b2, 0, b, b1.length, b2.length);
        return b;
    }


}