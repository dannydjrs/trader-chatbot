package virtualassistant.chatbot;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.*;

import java.lang.*;
import javafx.application.Platform;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Port;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.result.WordResult;

import virtualassistant.gui.Controller;

public class SpeechRecognizerMain {

	// Necessary
	private LiveSpeechRecognizer recognizer;

	// Logger
	private Logger logger = Logger.getLogger(getClass().getName());

	/**
	 * This String contains the Result that is coming back from SpeechRecognizer
	 */
	private String speechRecognitionResult;

	//-----------------Lock Variables-----------------------------

	/**
	 * This variable is used to ignore the results of speech recognition cause actually it can't be stopped...
	 *
	 * <br>
	 * Check this link for more information: <a href=
	 * "https://sourceforge.net/p/cmusphinx/discussion/sphinx4/thread/3875fc39/">https://sourceforge.net/p/cmusphinx/discussion/sphinx4/thread/3875fc39/</a>
	 */
	private boolean ignoreSpeechRecognitionResults = false;

	/**
	 * Checks if the speech recognise is already running
	 */
	private boolean speechRecognizerThreadRunning = false;

	/**
	 * Checks if the resources Thread is already running
	 */
	private boolean resourcesThreadRunning;

	//---

	/**
	 * This executor service is used in order the playerState events to be executed in an order
	 */
	private ExecutorService eventsExecutorService = Executors.newFixedThreadPool(2);

	//------------------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public SpeechRecognizerMain(Controller controller) {

		this.controller = controller;

		Logger rootLogger = LogManager.getLogManager().getLogger("");
		rootLogger.setLevel(Level.WARNING);
		for (Handler h : rootLogger.getHandlers()) {
		    h.setLevel(Level.WARNING);
		}

		// Loading Message
		//logger.log(Level.INFO, "Loading Speech Recognizer...\n");

		// Configuration
		Configuration configuration = new Configuration();

		// Load model from the jar
		configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        //System.out.println("Set Acoustic Model Path");
		configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        //System.out.println("Set Dictionary Path");
		//====================================================================================
		//=====================READ THIS!!!===============================================
		//Uncomment this line of code if you want the recognizer to recognize every word of the language
		//you are using , here it is English for example
		//====================================================================================
		//configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

		//====================================================================================
		//=====================READ THIS!!!===============================================
		//If you don't want to use a grammar file comment below 3 lines and uncomment the above line for language model
		//====================================================================================

		// Grammar
		configuration.setGrammarPath("./data/grammars");
		configuration.setGrammarName("grammar");
		configuration.setUseGrammar(true);
        //System.out.println("Set Grammar");

		try {
			recognizer = new LiveSpeechRecognizer(configuration);
            System.out.println("Grammar loaded!");
		} catch (IOException ex) {
			//logger.log(Level.SEVERE, null, ex);
            ex.printStackTrace();
		} catch (Exception e){
            e.printStackTrace();
        }
        //System.out.println("Started Resources Thread");

		// Start recognition process pruning previously cached data.
		// recognizer.startRecognition(true);

		//Check if needed resources are available
		startResourcesThread();
        System.out.println("Started Resources Thread");
		//Start speech recognition thread
		startSpeechRecognition();
        System.out.println("Started speech  recognition");
	}

	//-----------------------------------------------------------------------------------------------
	private Controller controller;
	/**
	 * Starts the Speech Recognition Thread
	 */
	public synchronized void startSpeechRecognition() {

        //Check lock
		if (speechRecognizerThreadRunning)
			System.out.println("Speech Recognition Thread already running...\n");
		else
			//Submit to ExecutorService
			eventsExecutorService.submit(() -> {

				//locks
				speechRecognizerThreadRunning = true;
				ignoreSpeechRecognitionResults = true;

				//Start Recognition
				try {
					recognizer.startRecognition(true);
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				//Information
				//System.out.println("You can start to speak...\n");

				try {
					while (speechRecognizerThreadRunning) {
						/*
						 * This method will return when the end of speech is reached. Note that the end pointer will determine the end of speech.
						 */
						SpeechResult speechResult = recognizer.getResult();

						//Check if we ignore the speech recognition results
						if (!ignoreSpeechRecognitionResults) {

							//Check the result
							if (speechResult == null)
								System.out.println("I can't understand what you said.\n");
							else {

								//Get the hypothesis
								speechRecognitionResult = speechResult.getHypothesis();

								//You said?
								System.out.println("You said: [" + speechRecognitionResult + "]\n");

								//Call the appropriate method
								makeDecision(speechRecognitionResult, speechResult.getWords());
								System.out.println("Decision made!");

							}
						} else {
                              System.out.println("Ingoring Speech Recognition Results...");
                        }
					}
				} catch (Exception ex) {
					//logger.log(Level.WARNING, null, ex);
                     ex.printStackTrace();
					speechRecognizerThreadRunning = false;
				}

				//logger.log(Level.INFO, "SpeechThread has exited...");

			});
	}

	/**
	 * Stops ignoring the results of SpeechRecognition
	 */
	public synchronized void stopIgnoreSpeechRecognitionResults() {

		//Stop ignoring speech recognition results
		ignoreSpeechRecognitionResults = false;
		System.out.println("No longer ignoring speech recognition results");
	}

	/**
	 * Ignores the results of SpeechRecognition
	 */
	public synchronized void ignoreSpeechRecognitionResults() {

		//Instead of stopping the speech recognition we are ignoring it's results
		ignoreSpeechRecognitionResults = true;
		System.out.println("Ignoring speech recognition results");

	}

	//-----------------------------------------------------------------------------------------------

	/**
	 * Starting a Thread that checks if the resources needed to the SpeechRecognition library are available
	 */
	public void startResourcesThread() {

		//Check lock
		if (resourcesThreadRunning)
			logger.log(Level.INFO, "Resources Thread already running...\n");
		else
			//Submit to ExecutorService
			eventsExecutorService.submit(() -> {
				try {

					//Lock
					resourcesThreadRunning = true;

					// Detect if the microphone is available
					while (true) {

						//Is the Microphone Available
						if (!AudioSystem.isLineSupported(Port.Info.MICROPHONE))
							//logger.log(Level.INFO, "Microphone is not available.\n");

						// Sleep some period
						Thread.sleep(350);
					}

				} catch (InterruptedException ex) {
					logger.log(Level.WARNING, null, ex);
					resourcesThreadRunning = false;
				}
			});
	}

	/**
	 * Takes a decision based on the given result
	 *
	 * @param speechWords
	 */
	public void makeDecision(String speech , List<WordResult> speechWords) {

        //System.out.println("Made decision: " + speech);
		try {
			Platform.runLater(new Runnable() {
				@Override public void run() {
					System.out.println("Making decision?");
                    controller.makeQuery(speech);
				}
			});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean getIgnoreSpeechRecognitionResults() {
		return ignoreSpeechRecognitionResults;
	}

	public boolean getSpeechRecognizerThreadRunning() {
		return speechRecognizerThreadRunning;
	}

	/**
	 * Main Method
	 *
	 * @param args
	 */
	// public static void main(String[] args) {
	// 	new SpeechRecognizerMain();
	// }
}
