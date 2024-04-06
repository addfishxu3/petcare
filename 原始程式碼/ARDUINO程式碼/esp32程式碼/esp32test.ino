  #include <ESP32Servo.h>
  #include <WiFi.h>
  #include <Firebase_ESP_Client.h>
  #include <SD.h>
  #include <SPI.h>
  #include <FS.h>
  #include "addons/TokenHelper.h"
  #include "addons/RTDBHelper.h"
  #include <time.h>
  #include <NTPClient.h>
  #include <WiFiUdp.h>

  #define WIFI_SSID ""//your wifi
  #define WIFI_PASSWORD ""//pwd
  #define API_KEY ""
  #define DATABASE_URL ""
  #define TOUCH_PIN 32
  #define USER_EMAIL "123@gmail.com"
  #define USER_PASSWORD "111111"
  String USER_PET="680";
 
  FirebaseData fdbo, fdbo_s1,fdbo_s2,fdbo_s3;
  FirebaseAuth auth;
  FirebaseConfig config;

  unsigned long sendDataPrevMillis = 0;
  String uid;

  Servo myServo;
  bool btnStatus = LOW;
  bool lastStatus = LOW;

  long timestamp;
  const char* ntpServer = "pool.ntp.org";
  unsigned long getTime() {
    time_t now;
    struct tm timeinfo;
    if (!getLocalTime(&timeinfo)) {
      Serial.println("Failed to obtain time");
      return(0);
    }
    time(&now);
    return now;
  }

  //現在時間及上次餵食時間
  String formattedDate;
  String timeStamp;
  int date,getdate;
  int hour,gethour;
  int minute,getminute;

  WiFiUDP ntpUDP;
  NTPClient timeClient(ntpUDP);

  //設定分量及取食間隔及自動餵食時間
  int feedamount;
  int houritv,minuteitv;
  int autohour,autominute;  

  void setup() {
    // put your setup code here, to run once:
    myServo.attach(33,500,2400);
    pinMode(TOUCH_PIN, INPUT);
    Serial.begin(9600);


    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    while(WiFi.status()!=WL_CONNECTED){
      delay(500);
      Serial.print(".");
    }
    Serial.println("已連上WiFi");
    //Serial.println(WiFi.macAddress());
    Serial.println(WiFi.localIP());
    
    config.api_key = API_KEY;
    auth.user.email = USER_EMAIL;
    auth.user.password = USER_PASSWORD;
    config.database_url = DATABASE_URL;

    config.token_status_callback = tokenStatusCallback;
    Firebase.begin(&config, &auth);
    while ((auth.token.uid) == "") {
    Serial.print('.');
    delay(1000);
    }
    uid = auth.token.uid.c_str();
    Firebase.reconnectWiFi(true);

    configTime(0, 0, ntpServer);
    timeClient.begin();
    timeClient.setTimeOffset(28800);// GMT +8 = 28800
    if(!Firebase.RTDB.beginStream(&fdbo_s1, "/User/"+uid+"/petList/"+USER_PET+"/SG90/switch"))
      Serial.printf("stream 1 begin error, %s\n\n", fdbo_s1.errorReason().c_str());
  }
  
  //判斷取食是否超過間隔時間
  void onSg90Change()  {
    Serial.println("onSg90Change");
    timeClient.update();
    hour=timeClient.getHours();
    //Serial.println (hour);
    minute=timeClient.getMinutes();
    //Serial.println (minute);
    time_t epochTime = timeClient.getEpochTime();
    struct tm *ptm = gmtime ((time_t *)&epochTime);
    date=ptm->tm_mday;
    //Serial.println(date);
    //Serial.println(hour);
    //Serial.println(minute);
    if(date==getdate){
      if(((hour*60*60)+(minute*60))-((gethour*60*60)+(getminute*60))>=((houritv*60*60)+(minuteitv*60))){
        onSg90ChangeByPet();
      }else{
         Serial.println ("reject");
      }
    }else{
       if(((hour*60*60)+(minute*60))+86400-((gethour*60*60)+(getminute*60))>=((houritv*60*60)+(minuteitv*60))){//86400一天秒數
         onSg90ChangeByPet();
       }else{
         Serial.println ("reject");
       }
    }
  }

  //寵物取食動作
  void onSg90ChangeByPet(){
    Firebase.RTDB.setString(&fdbo, "/User/"+uid+"/petList/"+USER_PET+"/SG90/switcher","by pet");
    Firebase.RTDB.setBool(&fdbo, "/User/"+uid+"/petList/"+USER_PET+"/SG90/switch", true);
    myServo.write(180);//轉到180度
    long delaytime=3000*feedamount;
    delay(delaytime);
    myServo.write(0);
    Firebase.RTDB.setBool(&fdbo, "/User/"+uid+"/petList/"+USER_PET+"/SG90/switch", false);
    timestamp = getTime();
    Serial.println (timestamp);
    Firebase.RTDB.setTimestamp(&fdbo, "/User/"+uid+"/petList/"+USER_PET+"/SG90/time");
    Firebase.RTDB.setInt(&fdbo, "/User/"+uid+"/petList/"+USER_PET+"/SG90/timedate",date);
    Firebase.RTDB.setInt(&fdbo, "/User/"+uid+"/petList/"+USER_PET+"/SG90/timehour",hour);
    Firebase.RTDB.setInt(&fdbo, "/User/"+uid+"/petList/"+USER_PET+"/SG90/timeminute",minute);
  }

  //飼主餵食動作
  void onSg90ChangeByUser()  {
    myServo.write(180);//轉到180度
    delay(3000*feedamount);
    myServo.write(0);
    Firebase.RTDB.setBool(&fdbo, "/User/"+uid+"/petList/"+USER_PET+"/SG90/switch", false);
  }

  //判斷自動餵食時間是否到了  
  void onSg90ChangeByAuto(){
    
    timeClient.forceUpdate();
    
    hour=timeClient.getHours();
    //Serial.println (hour);
    minute=timeClient.getMinutes();
    //Serial.println (minute);
    time_t epochTime = timeClient.getEpochTime();
    struct tm *ptm = gmtime ((time_t *)&epochTime);
    date=ptm->tm_mday;
    if(date==getdate){
      if(((hour*60*60)+(minute*60))-((gethour*60*60)+(getminute*60))>=((autohour*60*60)+(autominute*60))){
        onSg90ChangeByAutoSwitch();
      }
    }else{
       if(((hour*60*60)+(minute*60))+86400-((gethour*60*60)+(getminute*60))>=((autohour*60*60)+(autominute*60))){//86400一天秒數
         onSg90ChangeByAutoSwitch();
       }
    }
  }
  //自動餵食動作
  void onSg90ChangeByAutoSwitch(){
    if(Firebase.RTDB.getInt(&fdbo_s3, "/User/"+uid+"/petList/"+USER_PET+"/SG90/feedamount")){
      feedamount=fdbo_s3.intData();
    }
    Firebase.RTDB.setBool(&fdbo, "/User/"+uid+"/petList/"+USER_PET+"/SG90/switch", true);
    myServo.write(180);//轉到180度
    delay(3000*feedamount);
    myServo.write(0);
    Firebase.RTDB.setBool(&fdbo, "/User/"+uid+"/petList/"+USER_PET+"/SG90/switch", false);
    timestamp = getTime();
    Serial.println (timestamp);
    Firebase.RTDB.setTimestamp(&fdbo, "/User/"+uid+"/petList/"+USER_PET+"/SG90/time");
    Firebase.RTDB.setString(&fdbo, "/User/"+uid+"/petList/"+USER_PET+"/SG90/switcher","by auto");
    Firebase.RTDB.setInt(&fdbo, "/User/"+uid+"/petList/"+USER_PET+"/SG90/timedate",date);
    Firebase.RTDB.setInt(&fdbo, "/User/"+uid+"/petList/"+USER_PET+"/SG90/timehour",hour);
    Firebase.RTDB.setInt(&fdbo, "/User/"+uid+"/petList/"+USER_PET+"/SG90/timeminute",minute);    
  }
  void loop() {
    if (Firebase.isTokenExpired()){
      Firebase.refreshToken(&config);
      Serial.println("Refresh token");
    }
    if(Firebase.ready() && (millis() - sendDataPrevMillis > 5000 || sendDataPrevMillis == 0)){
      sendDataPrevMillis = millis();

      //-----------STORE STATE-----------
        if(touchRead(TOUCH_PIN)==HIGH){
        delay(2000);
        if(touchRead(TOUCH_PIN)==HIGH){
        btnStatus = touchRead(TOUCH_PIN);
          if(btnStatus==HIGH&&lastStatus==LOW){
            
              if(Firebase.RTDB.getInt(&fdbo_s3, "/User/"+uid+"/petList/"+USER_PET+"/SG90/feedamount")){
                feedamount=fdbo_s3.intData();
                Serial.println("touch1"); 
                if(Firebase.RTDB.getInt(&fdbo_s3, "/User/"+uid+"/petList/"+USER_PET+"/SG90/timedate")){
                  getdate=fdbo_s3.intData();
                  Serial.println("touch2"); 
                  if(Firebase.RTDB.getInt(&fdbo_s3, "/User/"+uid+"/petList/"+USER_PET+"/SG90/timehour")){
                    gethour=fdbo_s3.intData();
                    if(Firebase.RTDB.getInt(&fdbo_s3, "/User/"+uid+"/petList/"+USER_PET+"/SG90/timeminute")){
                      getminute=fdbo_s3.intData();
                      if(Firebase.RTDB.getInt(&fdbo_s3, "/User/"+uid+"/petList/"+USER_PET+"/SG90/hourinterval")){
                        houritv=fdbo_s3.intData();
                        if(Firebase.RTDB.getInt(&fdbo_s3, "/User/"+uid+"/petList/"+USER_PET+"/SG90/minuteinterval")){
                          minuteitv=fdbo_s3.intData();                      
                          onSg90Change(); 
                        }
                      }
                    }
                  }
                }
              }  
                    
            Serial.print("touch"); 
          }
        lastStatus=btnStatus;
        }
        }else{
          lastStatus=LOW;
        }
    }
    //---------READ ON CHANGE----------
    if(Firebase.ready()){
      if(!Firebase.RTDB.readStream(&fdbo_s1))
        Serial.printf("stream 1 read error, %s\n\n", fdbo_s1.errorReason().c_str());
      if(fdbo_s1.streamAvailable()){
        if(fdbo_s1.dataType() == "boolean"){
          if(fdbo_s1.boolData() == true){
            if(Firebase.RTDB.getString(&fdbo_s2, "/User/"+uid+"/petList/"+USER_PET+"/SG90/switcher")){
            if(fdbo_s2.stringData()=="by user"){
              if(Firebase.RTDB.getInt(&fdbo_s3, "/User/"+uid+"/petList/"+USER_PET+"/SG90/feedamount")){
                feedamount=fdbo_s3.intData();
                onSg90ChangeByUser();
              }         
            }
            }
          }
        }
      }
    }
     //---------AUTO FEED SETTING--------
    if(Firebase.ready()){
      if(Firebase.RTDB.getInt(&fdbo_s3, "/User/"+uid+"/petList/"+USER_PET+"/SG90/timedate")){
        getdate=fdbo_s3.intData();
        if(Firebase.RTDB.getInt(&fdbo_s3, "/User/"+uid+"/petList/"+USER_PET+"/SG90/timehour")){
          gethour=fdbo_s3.intData();
          if(Firebase.RTDB.getInt(&fdbo_s3, "/User/"+uid+"/petList/"+USER_PET+"/SG90/timeminute")){
            getminute=fdbo_s3.intData();
            if(Firebase.RTDB.getInt(&fdbo_s3, "/User/"+uid+"/petList/"+USER_PET+"/SG90/autohour")){
              autohour=fdbo_s3.intData();
              if(Firebase.RTDB.getInt(&fdbo_s3, "/User/"+uid+"/petList/"+USER_PET+"/SG90/autominute")){
                autominute=fdbo_s3.intData();                      
                onSg90ChangeByAuto();
              }
            }
          }
        }
      }
    }
  }
  

