package bg.singmaster.backend;

import java.util.ArrayList;

import android.util.Log;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.pitch.PitchDetectionHandler;
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;
import be.hogent.tarsos.dsp.pitch.PitchProcessor;

public class PitchExtractor implements PitchDetectionHandler {

	public PitchProcessor mPitchProcessor;
	
	DetectedPitch mCurrDetectedPitch;
	
	public ArrayList<DetectedPitch> mDetectedPitchArray;
	
	public PitchExtractor(int sampleRate, int bufferSize){
		PitchProcessor.PitchEstimationAlgorithm alg = 
				PitchProcessor.PitchEstimationAlgorithm.AMDF;
		
//		PitchProcessor.PitchEstimationAlgorithm alg = 
//				PitchProcessor.PitchEstimationAlgorithm.FFT_YIN;
		
		
		
		mPitchProcessor = new PitchProcessor(alg, sampleRate, bufferSize, this);
		
		
	}
	
	/***
	 * is called in be.hogent.tarsos.dsp.pitch.PitchProcessor.process()
	 * */
	public void handlePitch(final PitchDetectionResult pitchDetectionResult,
			AudioEvent audioEvent) {
		float currPitch = 0;
		double currTs = audioEvent.getTimeStamp();
		
		if (pitchDetectionResult.isPitched()) {
			currPitch = pitchDetectionResult.getPitch();
			
		}
	
		mCurrDetectedPitch = new DetectedPitch(currPitch, currTs );
		this.mDetectedPitchArray.add(mCurrDetectedPitch);
		
		Log.i("TAG", "pitch is " + currPitch);
		
		
	}
	
}
