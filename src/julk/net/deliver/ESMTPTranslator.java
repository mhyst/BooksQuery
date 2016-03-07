package julk.net.deliver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import sun.misc.BASE64Encoder;
import julk.net.mail.ESMTP;
import java.util.Calendar;

public class ESMTPTranslator extends Translator
{	
        public static String before = 
            "From: LibrosDB <jserranoortuno@wanadoo.es>\r\n"+
            "Reply-to: noreply <noreply@wanadoo.es>\r\n";
        public static String after = "X-Priority: 1\r\n"+
                                    "X-Mailer: DelivererMail [version 3.1]\r\n"+
                                    "X-Auto-Response-Suppress: All\r\n";
        
        public static String getDynamicHeader(String user) {
            //"Date: Tue, 10 Enet 2016 23:14:16 -0700\r\n"+
            //"To: Usuario <mhysterio@gmail.com>\r\n"+
            String hdr = "Date: ";
            hdr+= Calendar.getInstance().getTime() +"\r\n";
            hdr+= "To: "+user+"\r\n";
            
            return hdr;
        }

	public boolean translate (String user, String service,
							  String command, WorkResult wr)
	{
		int pos;
		String dest="";
		pos = command.indexOf("@");
		if (pos != -1) {
			dest = command;
		} else {
			pos = user.indexOf("@");
			if (pos != -1) {
				dest = user;
			} else {
				System.out.println("El usuario no indic� ninguna direcci�n de email.");
				return false;
			}
		}

		try {
			//WorkResult wr = wi.getWorkResult();
			BASE64Encoder b64encoder = new BASE64Encoder();
			FileInputStream _scriptFile;
			FileOutputStream _msgFile;
			System.out.println("Intentando enviar email a: "+command);
			_msgFile = new FileOutputStream("mailer.mme");
			//_msgFile.write("From: eMailer <deliverer@mipc.zapto.org>\r\n".getBytes());
			//_msgFile.write(("To:  <"+user+">\r\n").getBytes());
			//_msgFile.write(("To:  <"+dest+">\r\n").getBytes());
			
			if (wr != null) {
				File f = new File(wr.getName());
				String nombre = f.getName();
				/*String nombre = new String(wr.getName());
				pos = nombre.lastIndexOf("\\");
				if (pos != -1) nombre = nombre.substring(pos+1);
				pos = nombre.lastIndexOf("/");
				if (pos != -1) nombre = nombre.substring(pos+1);*/

				_scriptFile = new FileInputStream(wr.getName());
				if (wr.isAttached()) {
					//_msgFile.write(("From: "+user+"\r\n").getBytes());
					//_msgFile.write(("From: "++"\r\n").getBytes());
                                        _msgFile.write(ESMTPTranslator.getDynamicHeader(user).getBytes());
                                        _msgFile.write(before.getBytes());
					_msgFile.write("Subject: Respuesta a su petici�n\r\n".getBytes());
                                        _msgFile.write(after.getBytes());
					_msgFile.write("MIME-Version: 1.0\r\n".getBytes());
					_msgFile.write("Content-type: multipart/mixed; boundary=\"mailer-file-result\"\r\n".getBytes());
                                        _msgFile.write("\r\n".getBytes());
					_msgFile.write("\r\n--mailer-file-result\r\n".getBytes());
					_msgFile.write("Content-type: text/plain; charset=us-ascii\r\n\r\n".getBytes());
					_msgFile.write("Se ha completado una petici�n, vea los archivos adjuntos.\r\n".getBytes());
					_msgFile.write(("Trabajo finalizado\r\n").getBytes());
					_msgFile.write("\r\n--mailer-file-result\r\n".getBytes());
					_msgFile.write(("Content-Type: text/plain;name=\""+nombre+"\"\r\n").getBytes());
					_msgFile.write("Content-Transfer-Encoding: Base64\r\n".getBytes());
					_msgFile.write(("Content-Disposition: attachment;filename=\""+nombre+"\"\r\n\r\n").getBytes());
					//Codificaci�n base64
					b64encoder.encodeBuffer(_scriptFile, _msgFile);
					_msgFile.write("\r\n--mailer-file-result--\r\n".getBytes());			
				} else {
					int c;
					while ((c = _scriptFile.read()) != -1)
						_msgFile.write(c);
				}
				_scriptFile.close();
			} else {
                                _msgFile.write(ESMTPTranslator.getDynamicHeader(user).getBytes());
                                _msgFile.write(before.getBytes());
				_msgFile.write("Subject: Error de resultado\r\n".getBytes());
                                _msgFile.write(after.getBytes());
                                _msgFile.write("\r\n".getBytes());
				_msgFile.write(("Trabajo finalizado\r\n").getBytes());
				_msgFile.write("No se ha podido completar su petici�n\r\n".getBytes());
			}
			
			//_msgFile.write("\r\n.\r\n".getBytes());
			_msgFile.close();
			/*pos = user.indexOf("@");
			if (pos == -1)
				return false;
			*/
			//String smtpServer = "smtp." + wi.getUser().substring(pos+1);
			//String smtpServer = "mailhost.terra.es";
			MailConfig mail = getDeliverer().getMailConfig();
			ESMTP smtp = new ESMTP(mail.getSMTPserver(),mail.getSMTPport());
			smtp.ehlo("mailer");
			smtp.authLogin(mail.getESMTPuser(), mail.getESMTPpass());
			smtp.mail_from(mail.getEMail());
			//smtp.rcpt_to(user);
			smtp.rcpt_to(dest);
			smtp.dataFile("mailer.mme");
			smtp.quit();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("No se ha podido enviar el resultado a "+user);
			return false;
		}
	}
}
