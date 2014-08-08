package bg.singmaster.backend;

import java.util.ArrayList;

import android.util.Log;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.pitch.PitchDetectionHandler;
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;
import be.hogent.tarsos.dsp.pitch.PitchProcessor;

public class PitchExtractor implements PitchDetectionHandler {

	public PitchProcessor mPitchProcessor;
	
	public ArrayList<DetectedPitch> mDetectedPitchArray;
	
	public PitchExtractor(int sampleRate, int bufferSize){
		PitchProcessor.PitchEstimationAlgorithm alg = 
				PitchProcessor.PitchEstimationAlgorithm.AMDF;
		
		mPitchProcessor = new PitchProcessor(alg, sampleRate, bufferSize, this);
		
		
	}
	
	
	public void handlePitch(final PitchDetectionResult pitchDetectionResult,
			AudioEvent audioEvent) {
		float pitch = 0; 
		
		if (pitchDetectionResult.isPitched()) {
			pitch = pitchDetectionResult.getPitch();
			DetectedPitch currDetectedPitch = new DetectedPitch(pitch, audioEvent.getTimeStamp() );
			this.mDetectedPitchArray.add(currDetectedPitch);
		}
		
		Log.d("TAG", "pitch is " + pitch);
		
		
	}
	
}
