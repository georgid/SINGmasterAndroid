package bg.singmaster.gui;


import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import bg.singmaster.backend.AudioProcessor;
import bg.singmaster.gui.R;


public class TestActivity extends Activity {
	
	int SAMPLE_RATE=44100;
	AudioProcessor mAudioProcessor; 
	
	private TextView mPitchBox;
	
	private Button mListenButton;
//	private static final String TAG = "TestActivity";
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAudioProcessor = new AudioProcessor(SAMPLE_RATE);

		setContentView(R.layout.main);
		    
	
		mListenButton = (Button)findViewById(R.id.b1);
		
		   		
		
//		mPitchBox = (TextView)findViewById(R.id.pitch);

		
		
		
		
		
		mListenButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (mAudioProcessor.mIsRecording) {
					mAudioProcessor.stop();
					
					mListenButton.setText("Listen");
					
				} else {
					mAudioProcessor.record();
					mListenButton.setText("Stop listening");
				}
			}
		});
		
	}

}
