package ca.mcgill.ecse211.lab4;
import lejos.hardware.Button;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import static ca.mcgill.ecse211.lab4.Resources.*;
/**
 * @author zhaoliang & Brandon
 * Main function
 */
public class Main {
  
  public static void main (String[] args) throws InterruptedException {
    //Setup the Ultrasonic Sensor
    SensorModes usSensor = new EV3UltrasonicSensor(usPort);
    SampleProvider usValue = usSensor.getMode("Distance");
    float[] usData = new float[usValue.sampleSize()];   
    
    //Setup the Odometer and the Display
    Odometer odometer = Odometer.getOdometer();
    USLocalizer usLocalizer = new USLocalizer(leftMotor, rightMotor, odometer, usSensor, usData);
    Display display = new Display(odometer, usLocalizer);
    
    //Setup the Color Sensor
    @SuppressWarnings("resource")
    SensorModes colorSensor = new EV3ColorSensor(colorPort);
    SampleProvider colorValue = colorSensor.getMode("RGB");
    float[] colorData = new float[colorValue.sampleSize()];
    
    
    //Display on the screen defines here
    int pressForStage1;
    int pressForStage2;
    do {
      LCD.clear();
      
      LCD.drawString("< Left | Right >", 0, 0);
      LCD.drawString("       |        ", 0, 1);
      LCD.drawString(" do    | do     ", 0, 2);
      LCD.drawString(" Rising| Falling", 0, 3);
      LCD.drawString(" Edge  | Edge   ", 0, 4);
      pressForStage1 = Button.waitForAnyPress();
    } while (pressForStage1 != Button.ID_LEFT && pressForStage1 != Button.ID_RIGHT);
    //Do Rising Edge if left button is pressed
    if (pressForStage1 == Button.ID_LEFT) {
      odometer.start();
      display.start();
      usLocalizer.doLocalization("RISING_EDGE");
    }
    //Do Falling Edge if left button is pressed
    else {
      odometer.start();
      display.start();
      usLocalizer.doLocalization("FALLING_EDGE");
    }
    
    //Do localization to grid point(1,1), perform the light sensor localization.
    //wait for press before we enter the second stage.
    do {
      pressForStage2 = Button.waitForAnyPress();
    } while (pressForStage2 != Button.ID_LEFT && pressForStage2 != Button.ID_RIGHT);
    
    LightLocalizer lightLocalizer = new LightLocalizer(leftMotor, rightMotor, odometer, colorValue, colorData);
    Navigation navigation = new Navigation(leftMotor, rightMotor, odometer, lightLocalizer);//Navigating to grid point (1,1)
    lightLocalizer.start();
    navigation.doNavigation();
  }
}
