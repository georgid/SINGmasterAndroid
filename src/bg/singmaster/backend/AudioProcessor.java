package bg.singmaster.backend;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;


import be.hogent.tarsos.dsp.AudioEvent;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/***
 * 
 * records sound from mic
 * calls pitchExtractor from TarsosDSP
*/
public class AudioProcessor {
	
	private int mBufferSize;
	private byte[] mBuffer;
	
	private AudioRecord mRecorder;
	private int mSampleRate;
	
	public boolean mIsRecording = false;
	
	private be.hogent.tarsos.dsp.AudioFormat mTarsosFormat;
	
	private PitchExtractor mPitchExtractor; 
	
	private File mFile;
	FileOutputStream mOutStream = null;
	private static final int WAV_HEADER_LENGTH = 44;
	private String fileURI = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecordtest.wav";;

	
	// end file block
	
	public AudioProcessor(int sampleRate) {
		mSampleRate = sampleRate;
		
		mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		
		mBuffer = new byte[mBufferSize];
		
		mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				mSampleRate,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT,
				mBufferSize);
		
		mTarsosFormat = new be.hogent.tarsos.dsp.AudioFormat(
				(float)mSampleRate, 16, 1, true, false);
		
		mPitchExtractor = new PitchExtractor(mSampleRate, mBufferSize);
		
		
		mFile = createFile(fileURI);
	
	}
	
	/** trigger recording
	 * 
	 */
	public void record() {
		this.mPitchExtractor.mDetectedPitchArray = new ArrayList<DetectedPitch>(); 
		
		
		 if (mRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
		        
	    	  Log.e(WavRecorder.class.getName(), "Unable to access the audio recording hardware - is your mic working?");

	        return;
	      }
		
		mRecorder.startRecording();
		mIsRecording  = true;
		processAudio();
	}
	
	/**
	 * trigger stop of recording
	 * */
	public void stop() {
		mIsRecording = false;
		mRecorder.stop();
		finishWriteToFile();
	}
	
	/**
	 * record and call pitch extraction
	 * */
	private void processAudio() {
		Thread audioProcessingThread = new Thread(new AudioProcessingThreadWithFileRec());
		audioProcessingThread.start();
	}
	
	public class AudioProcessingThread implements Runnable{

		@Override
		public void run() {
			 // We're important...
		    	android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		    	
			while (mIsRecording) {
				int bufferReadResult = mRecorder.read(mBuffer, 0, mBufferSize);
				AudioEvent audioEvent = new AudioEvent(mTarsosFormat, bufferReadResult);
				audioEvent.setFloatBufferWithByteBuffer(mBuffer);
				
				mPitchExtractor.mPitchProcessor.process(audioEvent);
			}
			
		
		}// end run
	
	} // end AudioProcessigThread
	
	
	/**
	 * records audio from mic as well into file
	 * */
	public class AudioProcessingThreadWithFileRec implements Runnable{

		@Override
		public void run() {
			 // We're important...
		    	android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		    	
		    
		    	 
			while (mIsRecording) {
				int bufferReadResult = mRecorder.read(mBuffer, 0, mBufferSize);
				
				// record into file
				try {
					mOutStream.write(bufferReadResult);
				} 
				catch (final IOException e) {
			    	  Log.e(AudioProcessor.class.getName(), e.getMessage());

			      }
				
				AudioEvent audioEvent = new AudioEvent(mTarsosFormat, bufferReadResult);
				audioEvent.setFloatBufferWithByteBuffer(mBuffer);
				
			//	mPitchExtractor.mPitchProcessor.process(audioEvent);
			}
			
		
		}// end run
	
	} // end AudioProcessigThread
	
	
	public File createFile(String fileURI){
		
		 File outFile = new File(fileURI);
		      if (outFile.exists())
		        outFile.delete();

		      
		      try {
		        outFile.createNewFile();
		        
		        mOutStream = new FileOutputStream(outFile);
		        mOutStream.write(createHeader(0));// Write a dummy header for a file of length 0 to get updated later
		      } catch (Exception e) {
		    	  
		    	  Log.e(AudioProcessor.class.getName(), "unable to create file");
		    	  

		        return null;
		      }
		    return  outFile;
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
	    byte[] sampleRateBytes = intToBytes(this.mSampleRate);
	    byte[] bytesPerSecond = intToBytes(this.mSampleRate * 2);
	
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

	
	  public void finishWriteToFile(){
		  
		  try {
		        mOutStream.close();
		        
		      } catch (Exception e) {
		        e.printStackTrace();
		      }
		    
		   
		    if (this.mFile != null) {
		          appendHeader(mFile);
		
		          Intent scanWav = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		          scanWav.setData(Uri.fromFile(mFile));
	//DO NOT KNOW Why this does not import it from android.content.ContextWrapper
//		          android.content.ContextWrapper.sendBroadcast(scanWav);
		
		          //mFile = null;
		    }
		  
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
