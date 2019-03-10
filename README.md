# LetMeIn
 Let me in to aplikacja która za pomocą technologi Bluetooth umożliwa otwieranie zdalnego zamka.
 Cały projekt dzieli się na dwie strony:

 a) Klient - aplikacja stworzona pod system Android dzięki której użytkownik może łączyć się z serwerem i przesyłać odpowiednie dane.
 b) Serwer - program stworzony pod hardware Raspberry Pi. Pełna obsługa oparta na bibliotece Node.js. 

 Do stworzenia pełnej obsługi Bluetooth na system android został wykorzystany system BluetoothGatt stworzony przez google. Za pomocą tak zwanych Characteristics przesyła się odpowiednie dane markowane znacznikami odczytu, zapisu bądź ciągłej notyfikacji.

 Serwer oparty jest na opensource'owym systemie Bleno do pełnej obslugi Bluetooth po stronie Raspberry.

 Do obsługi wyjść GPIO wykorzystałem bibliotekę opensource'owa onoff.


 Gdy klient próbuje połączyć się z serwerem, weryfikuje on czy użytkownik ( jego UUID - specialny znacznik bluetooth ) jest już zwerfykiowany. Jeśli nie, wymaga on pinu który tworzy się przy inicializacji urządzenia. Jeżli PIN jest zgodny automatycznie dodaje użytkownika do bazy zwerfykiowanych użytkowników dzięki czemu nie należy wymagany jest już PIN w późniejszych etapach łączenia.


Instrukcja instalacji serwera:
1. Instalacja npm. Wpisujemy w terminalu:
    a) curl -sL http://deb.nodesource.com/setup_8.x | sudo bash -
    b) sudo apt-get install nodejs
2. Instalacja potrzebnych bibliotek:
    a) Do hashowania PINu :
        npm i bcrypt
    b) Obsługa bluetooth na raspberry :
        npm i bleno
    c) Zczytawanie danych w terminalu :
        npm i readline
    d) Sterowanie wyjściami fizycznymi :
        npm i onoff
    d) Obsługa plików systemowych :
        npm i fs

Android App interface:
![alt text](https://github.com/Raznyy/LetMeIn/blob/master/ReadmeImages/androidApp.png)


RaspberryPi interface:
![alt text](https://github.com/Raznyy/LetMeIn/blob/master/ReadmeImages/SerwerApp.png)