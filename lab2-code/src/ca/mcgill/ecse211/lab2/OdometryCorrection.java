package ca.mcgill.ecse211.lab2;

/**
 * This class implements the odometerCorrection for Lab2 on the EV3 Platform 
 * 
 * @author Freiberger & Zhao
 */

import static ca.mcgill.ecse211.lab2.Resources.*;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class OdometryCorrection implements Runnable {
  
  private float lastColor;
  private float currentColor;
  
  private static final long CORRECTION_PERIOD = 10;
 
  private SampleProvider lsColor = colorSensor.getMode("Red");
  private float color[] = new float[Resources.colorSensor.sampleSize()];
  private double lastPosition[] = new double[3];
  
  private double correction = 0;
  private int xNumLines = 0, yNumLines = 0;

  /*
   * Here is where the odometer correction code should be run.
   * This is the correction loop checks for black lines. depending on its orientation, it increments
   * or decrements the x or y line counter, then readjusts the robots x and y values on 
   * the display. 
   */
  public void run() {
    long correctionStart, correctionEnd;
    //this is for storing the previous value for the color intensity, to be used as a reference
    //value for indicating when the robot has crossed a black line.
    lsColor.fetchSample(color, 0);
    lastColor = color[0];
    
    
    
    while (true) {
      correctionStart = System.currentTimeMillis();
      
      lsColor.fetchSample(color, 0);
      //the current color recorded from the sensor
      currentColor = color[0];
      lastPosition = odometer.getXYT();
      //array index problem

      //we find the difference between the previous color and the current color, and if the
      //intensity difference is greater than the pre-determined difference of 5, we adjust
      //the relative x and y values. We determine which parameter (x or y) that we are adjusting
      //based on the theta value of the robot e.g. if the robot is pointing in the positive y
      //direction (between 340 and 20 degrees), the y line counter will be adjusted. The 
      //factor being added to the correction is the offset, which accounts for the error. 
      if (Math.abs(lastColor - currentColor)* 100 > 5) {
        Sound.beep();
        if (lastPosition[2] > 340 || lastPosition[2] < 20) {
          yNumLines++;
          correction = TILE_SIZE * yNumLines + 10;
          odometer.setY(correction);
        }
        else if (lastPosition[2] > 70 && lastPosition[2] < 110) {
          xNumLines++;
          correction = TILE_SIZE * xNumLines + 1.0;
          odometer.setX(correction);
        }
        else if (lastPosition[2] > 160 && lastPosition[2] < 200) {
          correction = TILE_SIZE * yNumLines + 10;
          odometer.setY(correction);
          yNumLines--;
        }
        else if (lastPosition[2] > 250 && lastPosition[2] < 290) {
          correction = TILE_SIZE * xNumLines + 1.0;
          odometer.setX(correction);
          xNumLines--;
        }
      }

      // this ensures the odometry correction occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        Main.sleepFor(CORRECTION_PERIOD - (correctionEnd - correctionStart));
      }
    }
  }
  
}
