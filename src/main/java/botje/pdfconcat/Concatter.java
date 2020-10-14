package botje.pdfconcat;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Concatter {

	final static Logger logger = LoggerFactory.getLogger(Concatter.class);
	
	public static void main(String[] args) {
						
		logger.debug( "start met concat pdf bestanden" );	
        String SQL_SELECT = "SELECT PON.PDF FROM PDF_OBJECTEN PON WHERE PON.SOORT = 'INVOER' AND PON.GROEP = 1";
	    try ( Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@//localhost:21521/botje", "pdfconcat", "cde3cde3");
	       PreparedStatement preparedStatement = conn.prepareStatement(SQL_SELECT)) {
	       ResultSet resultSet = preparedStatement.executeQuery();
	       PDFMergerUtility ut = new PDFMergerUtility();
	       
	       LinkedList<InputStream> isll = new LinkedList<InputStream>();
	       int teller = 1;
	       String mergedFileName = "pdfbestand_jvo_" + teller++ + "merged.pdf";
	       while (resultSet.next()) {	    	   
	    	  //String naamPdfBestand = "pdfbestand_jvo_" + teller++ + ".pdf"; 
	          //File image = new File(naamPdfBestand);
	          //FileOutputStream fos = new FileOutputStream(image);
	          //byte[] buffer = new byte[1];
	          InputStream is = resultSet.getBinaryStream("PDF");
	          isll.add(is);
	          //ut.addSource(is);

	          //while (is.read(buffer) > 0) {
	          //  fos.write(buffer);
	          //}
	          //fos.close();	          	          	          
	          //logger.info("bestand {} gemaakt", naamPdfBestand );
	          //if (teller++==3) break;
	       }
	       ut.setDestinationFileName(mergedFileName);
	       ut.addSources(isll);		       
	       ut.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
	       
	       //OutputStream os = new FileOutputStream("pdfbestand_jvo_" + teller++ + "merged.pdf");
	       //PdfFileEditor fileEditor = new PdfFileEditor();
	       //Object[] objectRij = isll.toArray();
	       //int grootte = objectRij.length;
	       //InputStream[] isRij = new InputStream[grootte];
	       //for (int i=0; i<grootte; i++) {
	    	//   isRij[i] = (InputStream) objectRij[i];
	       //}
	       //fileEditor.concatenate(isRij,os);
	       	       	    	       
	       PreparedStatement pstmt = conn.prepareStatement("insert into pdf_objecten ( groep, soort, pdf ) values ( ?, ?, ? )");	       	            
	       File blob = new File(mergedFileName);
	       FileInputStream fis = new FileInputStream(blob);
	       pstmt.setInt(1, 2);  // set the PK value
	       pstmt.setString(2, "UITVOER");
	       pstmt.setBinaryStream(3, fis, (int)blob.length());
   		   pstmt.executeUpdate();
	       conn.commit();
	       //pstmt.close();	       	       	       
	    } catch (SQLException e) {	       
	       System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
	       logger.error("sql error {}",e.getMessage() );
	    } catch (Exception e) {
		   logger.error("ErrOR {}",e.getMessage() );	    	
	       e.printStackTrace();	       
	    }						
		
	}

}
