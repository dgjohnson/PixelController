package com.neophob.sematrix.listener;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.neophob.sematrix.effect.Threshold;
import com.neophob.sematrix.effect.Tint;
import com.neophob.sematrix.effect.Effect.EffectName;
import com.neophob.sematrix.generator.Blinkenlights;
import com.neophob.sematrix.generator.Image;
import com.neophob.sematrix.generator.ImageZoomer;
import com.neophob.sematrix.generator.TextureDeformation;
import com.neophob.sematrix.generator.Textwriter;
import com.neophob.sematrix.generator.Generator.GeneratorName;
import com.neophob.sematrix.glue.Collector;
import com.neophob.sematrix.glue.Shuffler;
import com.neophob.sematrix.properties.PropertiesHelper;

public class MessageProcessor {

	public enum ValidCommands {
		STATUS,
		CHANGE_GENERATOR_A,
		CHANGE_GENERATOR_B,
		CHANGE_EFFECT_A,
		CHANGE_EFFECT_B,
		CHANGE_MIXER,
		CHANGE_OUTPUT,
		CHANGE_OUTPUT_EFFECT,
		CHANGE_FADER,
		CHANGE_TINT,
		CHANGE_PRESENT,
		CHANGE_SHUFFLER_SELECT,
		CHANGE_THRESHOLD_VALUE,
		SAVE_PRESENT,
		LOAD_PRESENT,
		BLINKEN,
		IMAGE,
		IMAGE_ZOOMER,
		TEXTDEF,
		TEXTDEF_FILE,
		TEXTWR,
		//used for enable/disable random mode
		RANDOM,
		//used as a one shot randomizer
		RANDOMIZE
	}

	private static Logger log = Logger.getLogger(MessageProcessor.class.getName());
	
	private static final String IGNORE_COMMAND = "Ignored command";

	private MessageProcessor() {
		//no instance
	}

	/**
	 * process message from gui
	 * @param msg
	 * @param startFader
	 * @return STATUS if we need to send updates back to the gui (loaded preferences)
	 */
	public static synchronized ValidCommands processMsg(String[] msg, boolean startFader) {
		if (msg==null || msg.length<1) {
			return null;
		}

		int tmp;
		
		try {			
			ValidCommands cmd = ValidCommands.valueOf(msg[0]);
			Collector col = Collector.getInstance();
			switch (cmd) {
			case STATUS:
				return ValidCommands.STATUS;

			case CHANGE_GENERATOR_A:
				try {
					int size = col.getAllVisuals().size();
					for (int i=0; i<size; i++) {
						tmp=Integer.parseInt(msg[i+1]);
						col.getVisual(i).setGenerator1(tmp);
					}
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;
				
			case CHANGE_GENERATOR_B:
				try {
					int size = col.getAllVisuals().size();
					for (int i=0; i<size; i++) {
						tmp=Integer.parseInt(msg[i+1]);
						col.getVisual(i).setGenerator2(tmp);
					}
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_EFFECT_A:
				try {
					int size = col.getAllVisuals().size();
					for (int i=0; i<size; i++) {
						tmp=Integer.parseInt(msg[i+1]);
						col.getVisual(i).setEffect1(tmp);
					}
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_EFFECT_B:
				try {					
					int size = col.getAllVisuals().size();
					for (int i=0; i<size; i++) {
						tmp=Integer.parseInt(msg[i+1]);
						col.getVisual(i).setEffect2(tmp);
					}
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_MIXER:
				try {
					int size = col.getAllVisuals().size();
					for (int i=0; i<size; i++) {
						tmp=Integer.parseInt(msg[i+1]);
						col.getVisual(i).setMixer(tmp);
					}
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_OUTPUT:
				try {
					int size = col.getAllOutputMappings().size();
					for (int i=0; i<size; i++) {
						int newFx = Integer.parseInt(msg[i+1]);
						int oldFx = col.getFxInputForScreen(i);
						if(oldFx!=newFx) {
							log.log(Level.INFO,	"Change Output 0, old fx: {0}, new fx {1}", new Object[] {oldFx, newFx});
							if (startFader) {
								//start fader to change screen
								col.getOutputMappings(i).getFader().startFade(newFx, i);								
							} else {
								//do not fade if we load setting from present
								col.mapInputToScreen(i, newFx);
							}
						}
					}
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_OUTPUT_EFFECT:
				try {
					int size = col.getAllOutputMappings().size();
					for (int i=0; i<size; i++) {
						tmp=Integer.parseInt(msg[i+1]);
						col.getOutputMappings(i).setEffect(col.getEffect(tmp));
					}
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case CHANGE_FADER:
				try {
					int size = col.getAllOutputMappings().size();
					for (int i=0; i<size; i++) {
						tmp=Integer.parseInt(msg[i+1]);
						//do not start a new fader while the old one is still running
						if (!col.getOutputMappings(i).getFader().isStarted()) {
							col.getOutputMappings(i).setFader(col.getFader(tmp));							
						}
					}
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;
				
			case CHANGE_SHUFFLER_SELECT:
				try {					
					int size = col.getShufflerSelect().size();
					boolean b;
					for (int i=0; i<size; i++) {
						b = false;
						if (msg[i+1].equals("1")) b = true;
						col.setShufflerSelect(i, b);
					}					
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;
				
			case CHANGE_TINT:
				try {					
					int r = Integer.parseInt(msg[1]);
					int g = Integer.parseInt(msg[2]);
					int b = Integer.parseInt(msg[3]);
					if (r>255) r=255;
					if (g>255) g=255;
					if (b>255) b=255;
					if (r<0) r=0;
					if (g<0) g=0;
					if (b<0) b=0;
					col.setRGB(r, g, b);
					Tint t = (Tint)col.getEffect(EffectName.TINT);
					t.setColor(r, g, b);
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case SAVE_PRESENT:
				try {
					int idxs = col.getSelectedPresent();
					List<String> present = col.getCurrentStatus();
					col.getPresent().get(idxs).setPresent(present);
					PropertiesHelper.savePresents();
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case LOAD_PRESENT:
				try {
					int idxl = col.getSelectedPresent();
					List<String> present = col.getPresent().get(idxl).getPresent();
					if (present!=null) { 
						col.setCurrentStatus(present);
					}
					return ValidCommands.STATUS;					
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;
				
			case CHANGE_PRESENT:
				try {
					int a = Integer.parseInt(msg[1]);
					col.setSelectedPresent(a);
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;
				
			case CHANGE_THRESHOLD_VALUE:
				try {
					int a = Integer.parseInt(msg[1]);
					if (a>255) a=255;
					if (a<0) a=0;
					col.setThresholdValue(a);
					Threshold t = (Threshold)col.getEffect(EffectName.THRESHOLD);
					t.setThreshold(a);
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;
				
			case BLINKEN:
				try {
					String fileToLoad = msg[1];
					col.setFileBlinken(fileToLoad);
					Blinkenlights blink = (Blinkenlights)col.getGenerator(GeneratorName.BLINKENLIGHTS);
					blink.loadFile(fileToLoad);
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case IMAGE:
				try {
					String fileToLoad = msg[1];
					col.setFileImageSimple(fileToLoad);
					Image img = (Image)col.getGenerator(GeneratorName.IMAGE);
					img.loadFile(fileToLoad);
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case IMAGE_ZOOMER:
				try {
					String fileToLoad = msg[1];
					col.setFileImageZoomer(fileToLoad);
					ImageZoomer img = (ImageZoomer)col.getGenerator(GeneratorName.IMAGE_ZOOMER);
					img.loadImage(fileToLoad);
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;
				
			case TEXTDEF:
				try {
					int lut = Integer.parseInt(msg[1]);
					col.setTextureDeformationLut(lut);
					TextureDeformation df = (TextureDeformation)col.getGenerator(GeneratorName.TEXTURE_DEFORMATION);
					df.changeLUT(lut);
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;
			
			case TEXTDEF_FILE:
				try {
					String fileToLoad = msg[1];
					col.setFileTextureDeformation(fileToLoad);
					TextureDeformation df = (TextureDeformation)col.getGenerator(GeneratorName.TEXTURE_DEFORMATION);
					df.loadFile(fileToLoad);
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;
			
			case TEXTWR:
				try {
					String message = msg[1];
//					System.out.println(message);
					col.setText(message);
					Textwriter txt = (Textwriter)col.getGenerator(GeneratorName.TEXTWRITER);
					txt.createTextImage(message);
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;
				
			case RANDOM:
				try {
					String onOrOff = msg[1];
					if (onOrOff.equalsIgnoreCase("ON")) {
						col.setRandomMode(true);
					}
					if (onOrOff.equalsIgnoreCase("OFF")) {
						col.setRandomMode(false);
						return ValidCommands.STATUS;
					}
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;

			case RANDOMIZE:
				try {
					Shuffler.manualShuffleStuff();
					return ValidCommands.STATUS;
				} catch (Exception e) {
					log.log(Level.WARNING,	IGNORE_COMMAND, e);
				}
				break;
				
			default:
				String s="";
				for (int i=0; i<msg.length;i++) {
					s+=msg[i]+"; ";
				}
				log.log(Level.INFO,	"Ignored command <{0}>", s);
				break;
			}
		} catch (IllegalArgumentException e) {
			log.log(Level.INFO,	"Illegal argument <{0}>: {1}", new Object[] { msg[0], e });
			e.printStackTrace();
		}		

		return null;
	}
}
