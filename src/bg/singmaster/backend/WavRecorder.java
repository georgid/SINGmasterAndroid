package bg.singmaster.backend;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;



import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.app.Activity;
import android.content.Intent;
import android.content.ContextWrapper;

/**
 * Recording of wav without THREADS. Simplified. records only n=10 seconds without giving the user chance to stop
 * Contains logic copied from Hertz recorder.
 * https://github.com/ucam-cl-dtg/hertz 
 *  full tutorial here: http://www.devlper.com/2010/12/android-audio-recording-part-2/
 * */
public class WavRecorder {

	public final int sampleRate = 44100;  
	
	public File outFile; 
	public String fileName; 
    
	// record for 5 seconds;
    public int seconds = 10;   
	
    @Deprecated
	private boolean isListening;
	  
	 private static final int WAV_HEADER_LENGTH = 44;
	  private final int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	    private final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	
	    
	    
	    
		/**
		   * Capture raw audio data from the hardware and saves it to a buffer in the enclosing class.
		   * 
		   * @author Rhodri Karim
		   * 
		   */
		  public class Capture implements Runnable {


		    // the actual output format is big-endian, signed
		  		    public void run(){
		  		    	
		  		    	 // We're important...
		  		    	android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		  		    	 int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding);

		  			      int bufferSize = 2 * minBufferSize;
		  			      AudioRecord recordInstance =
		  			          new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioEncoding,
		  			              bufferSize);
		  			     
		  			      if (recordInstance.getState() != AudioRecord.STATE_INITIALIZED) {
		  			        
		  			    	  Log.e(WavRecorder.class.getName(), "Unable to access the audio recording hardware - is your mic working?");
		  			    	  

		  			        return;
		  			      }

		  			      byte[] tempBuffer = new byte[bufferSize];

		  			    
		  			      outFile = new File(fileName);
		  			      if (outFile.exists())
		  			        outFile.delete();

		  			      FileOutputStream outStream = null;
		  			      try {
		  			        outFile.createNewFile();
		  			        outStream = new FileOutputStream(outFile);
		  			        outStream.write(createHeader(0));// Write a dummy header for a file of length 0 to get updated later
		  			      } catch (Exception e) {
		  			    	  
		  			    	  Log.e(WavRecorder.class.getName(), "unable to create file");
		  			    	  

		  			        return;
		  			      }

		  			      recordInstance.startRecording();
/**
 *	recording cycle	  			      	  			      
*/		  			      try {
		  			    	  
		  			    	  Date start = new Date();
		  			    	    Date end = new Date();  
		  			        // let record for some seconds
		  			    	 while (end.getTime() - start.getTime() < seconds * 1000) {
		  			          // record into buffer
		  			        	recordInstance.read(tempBuffer, 0, bufferSize);
		  			          outStream.write(tempBuffer);
		  			          // update time 
		  			          end = new Date();
		  			        }
		  			      } catch (final IOException e) {
		  			    	  Log.e(WavRecorder.class.getName(), e.getMessage());

		  			      } catch (OutOfMemoryError om) {
		  			    	  Log.e(WavRecorder.class.getName(), "out of memory");

		  			      }

		  			      // we're done recording
		  			      Log.d("Capture", "Stopping recording");
		  			      recordInstance.stop();
		  			      try {
		  			        outStream.close();
		  			        
		  			      } catch (Exception e) {
		  			        e.printStackTrace();
		  			      }
		  			    
		  			    isListening = false;
		  			   
		  			    finalizeWriteToFile();
		  			      
		  		    } // end of run
		  		   	
		  } //end of inner class Capture
		  
	
	public void beginRecording(String fileName){
		
		this.fileName = fileName; 
		
		 isListening = true;
		 
		 Thread t = new Thread(new Capture());
		    t.start();
		    
		    
		  
		 
		
	}
	
	
	/**
	   * End the recording, saving and finalising the file
	   */
	  public void finalizeWriteToFile() {
	    
	
	    if (isListening == false && outFile != null) {
	          appendHeader(outFile);
	
	          Intent scanWav = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	          scanWav.setData(Uri.fromFile(outFile));
//DO NOT KNOW Why this does not import it from android.content.ContextWrapper
//	          android.content.ContextWrapper.sendBroadcast(scanWav);
	
	          outFile = null;
	    }
	  }
	  
	  



	/**
	   * Creates a valid WAV header for the given bytes, using the class-wide sample rate
	   * 
	   * @param bytes The sound data to be appraised
	   * @return The header, ready to be written to a file
	   */
	  public byte[] createHeader(int bytesLength) {
	
	    int totalLength = bytesLength + 4 + 24 + 8;
	    byte[] lengthData = intToBytes(totalLength);
	    byte[] samplesLength = intToBytes(bytesLength);
	    byte[] sampleRateBytes = intToBytes(this.sampleRate);
	    byte[] bytesPerSecond = intToBytes(this.sampleRate * 2);
	
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	
	    try {
	      out.write(new byte[] {'R', 'I', 'F', 'F'});
	      out.write(lengthData);
	      out.write(new byte[] {'W', 'A', 'V', 'E'});
	
	      out.write(new byte[] {'f', 'm', 't', ' '});
	      out.write(new byte[] {0x10, 0x00, 0x00, 0x00}); // 16 bit chunks
	      out.write(new byte[] {0x01, 0x00, 0x01, 0x00}); // mono
	      out.write(sampleRateBytes); // sampling rate
	      out.write(bytesPerSecond); // bytes per second
	      out.write(new byte[] {0x02, 0x00, 0x10, 0x00}); // 2 bytes per sample
	
	      out.write(new byte[] {'d', 'a', 't', 'a'});
	      out.write(samplesLength);
	    } catch (IOException e) {
	      Log.e("Create WAV", e.getMessage());
	    }
	
	    return out.toByteArray();
	  }



	/**
	   * Turns an integer into its little-endian four-byte representation
	   * 
	   * @param in The integer to be converted
	   * @return The bytes representing this integer
	   */
	  public static byte[] intToBytes(int in) {
	    byte[] bytes = new byte[4];
	    for (int i = 0; i < 4; i++) {
	      bytes[i] = (byte) ((in >>> i * 8) & 0xFF);
	    }
	    return bytes;
	  }



	/**
	   * Appends a WAV header to a file containing raw audio data. Uses different strategies depending
	   * on amount of free disk space.
	   * 
	   * @param file The file containing 16-bit little-endian PCM data.
	   */
	  public void appendHeader(File file) {
	
	    int bytesLength = (int) file.length();
	    byte[] header = createHeader(bytesLength - WAV_HEADER_LENGTH);
	
	    try {
	      RandomAccessFile ramFile = new RandomAccessFile(file, "rw");
	      ramFile.seek(0);
	      ramFile.write(header);
	      ramFile.close();
	    } catch (FileNotFoundException e) {
	      Log.e("Hertz", "Tried to append header to invalid file: " + e.getLocalizedMessage());
	      return;
	    } catch (IOException e) {
	      Log.e("Hertz", "IO Error during header append: " + e.getLocalizedMessage());
	      return;
	    }
	
	  }
}
