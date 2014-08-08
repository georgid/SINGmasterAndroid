package bg.singmaster.backend;

public class DetectedPitch {

	public float mPitchHz;
	public double mTimeStamp; 
	
	public DetectedPitch(float pitchHz, double timeStamp){
		this.mPitchHz = pitchHz; 
		this.mTimeStamp = timeStamp;
	}
}
