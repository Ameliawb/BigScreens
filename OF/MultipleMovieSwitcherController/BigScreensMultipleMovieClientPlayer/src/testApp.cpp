#include "testApp.h"

//--------------------------------------------------------------
void testApp::setup(){

	// Load initial settings
	
	started = false;
	fullscreen = false;
    looping = false;
	ofSetVerticalSync(true);
	
    loadSettings("settings.xml");
	
	// Set up OSC
	receiver.setup( port );
	std::cout << "listening for osc messages on port " << port << "\n";
	
	
	// Load movie
    for(int i = 0; i < NUM_MOVIES; i++) {
        std::cout << movieFiles[i];
        movies[i].loadMovie(movieFiles[i]);
        movies[i].play();
        movies[i].update();
    }
    state = 0;
    
    //movie.setLoopState(OF_LOOP_NORMAL);
	
	
	ofBackground( 0, 0, 0 );

}

//--------------------------------------------------------------
void testApp::update(){
    // Load movie
    for(int i = 0; i < NUM_MOVIES; i++) {
        movies[i].setPaused(state != i);
        cout << "Movie " << i << " is" << (movies[i].isPaused() ? " not" : "") << " playing.\n";
    }
    
	if (started) {
		movies[state].update();
	}
	
	
	// Check for waiting messages
	while( receiver.hasWaitingMessages() )
	{
		// Get the next message
		ofxOscMessage m;
		receiver.getNextMessage( &m );
		
		// Check for movie position info
		if ( m.getAddress() == "/movie/position" )
		{
			float p = m.getArgAsFloat(0);
			if (!started) {
				started = true;
				movies[state].play();
                // Turns looping off
                if (!looping) {
                    movies[state].setLoopState(OF_LOOP_NONE);
                } else {
                    movies[state].setLoopState(OF_LOOP_NORMAL);
                }
                
                
				// No need to loop, we loop if the master loops
				//movie.setLoopState(OF_LOOP_NONE);
			}
			// Set the position
			movies[state].setPosition(p);
			
		}
        else if( m.getAddress() == "/movie/state" ) {
            state = m.getArgAsInt32(0);
        }
		
	}}

//--------------------------------------------------------------
void testApp::draw(){
	if (fullscreen) {
		ofHideCursor();
	}
	movies[state].draw(movieX,movieY,movieWidth,movieHeight);
	
	// Display some debugging info
	//char buf[256];
	//sprintf( buf, "listening for osc messages on port %d", port );
	//ofDrawBitmapString( buf, 10, 20 );
}

//--------------------------------------------------------------
void testApp::keyPressed(int key){
}

//--------------------------------------------------------------
void testApp::keyReleased(int key){

}

//--------------------------------------------------------------
void testApp::mouseMoved(int x, int y){

}

//--------------------------------------------------------------
void testApp::mouseDragged(int x, int y, int button){

}

//--------------------------------------------------------------
void testApp::mousePressed(int x, int y, int button){

}

//--------------------------------------------------------------
void testApp::mouseReleased(int x, int y, int button){

}

//--------------------------------------------------------------
void testApp::windowResized(int w, int h){

}

//--------------------------------------------------------------
void testApp::gotMessage(ofMessage msg){

}

//--------------------------------------------------------------
void testApp::dragEvent(ofDragInfo dragInfo){

}

void testApp::loadSettings(string fileString){
	
	
	//--------------------------------------------- get configs
    ofxXmlSettings xmlReader;
	
	bool result = xmlReader.loadFile(fileString);
	
	if(!result) printf("error loading xml file\n");
	
	int w = xmlReader.getValue("settings:dimensions:width", 640, 0);
	int h = xmlReader.getValue("settings:dimensions:height", 480, 0);
	
	movieWidth = xmlReader.getValue("settings:dimensions:movieWidth", 640, 0);
	movieHeight = xmlReader.getValue("settings:dimensions:movieHeight", 480, 0);
	
	movieX = xmlReader.getValue("settings:dimensions:movieX", 640, 0);
	movieY = xmlReader.getValue("settings:dimensions:movieY", 480, 0);
	
	port = xmlReader.getValue("settings:port",9999,0);
	
	xmlReader.pushTag("settings");
    for(int i = 0; i < NUM_MOVIES; i++) {
        string filename = xmlReader.getValue("movie:","test",i);
        movieFiles[i] = (char *) malloc(sizeof(char)*filename.length());
        strcpy(movieFiles[i], filename.c_str());
        cout << "movie loaded: " << filename << "\n";
    }
	xmlReader.popTag();
	
	ofSetWindowShape(w, h);
	
	if(xmlReader.getValue("settings:go_fullscreen", "false", 0).compare("true") == 0) {
		fullscreen = true;
		ofSetFullscreen(true);
	}
    
    if(xmlReader.getValue("settings:loop", "false", 0).compare("true") == 0) {
		looping = true;
	}
	
	
}


