Balabolka (aplicaci�n de consola), versi�n 1.57
Copyright (c) 2013-2019 Ilya Morozov
Todos los derechos reservados

WWW: http://balabolka.site/es/bconsole.htm
E-mail: crossa@list.ru

Licencia: Gratuito (Freeware)
Sistema operativo: Microsoft Windows XP/Vista/7/8/10
Microsoft Speech API: 4.0/5.0 y superiores
Microsoft Speech Platform: 11.0



*** Uso ***

balcon [opciones ...]


*** Opciones de la l�nea de comandos ***

-l
   Muestra la lista de voces disponibles.

-g
   Muestra la lista de dispositivos de salida de audio disponibles.

-f <archivo de texto>
   Establece el nombre del archivo de texto de entrada.

-w <archivo de onda>
   Establece el nombre del archivo de salida en formato WAV. Si se especifica la opci�n, se crear� un archivo de audio. De lo contrario, el texto ser� le�do en voz alta.

-n <nombre_de_voz>
   Nombre de la voz (basta especificar una parte del nombre). Si no se especifica, se usar� la voz seleccionada en el panel de control de Windows.

-id <entero>
   Establece el identificador de idioma (Locale ID) de la voz. El identificador de idioma es el c�digo de idioma asignado por Microsoft
   (por ejemplo, "1033" o "0x0409" para "ingl�s - Estados Unidos", "3082" o "0x0C0A" para "espa�ol - alfabetizaci�n internacional").
   El programa escoger� de la lista la primera voz cuyo identificador de idioma coincida con el valor especificado.
   Si el par�metro no est� especificado, entonces se utilizar� la voz, especificada con ayuda del par�metro [-n], o la voz seleccionada en el panel de control de Windows.
   Lista de identificadores de idioma: https://msdn.microsoft.com/en-us/library/cc233982.aspx

-m
   Muestra los par�metros de la voz.

-b <entero>
   Establece el dispositivo de salida de audio por su �ndice. El �ndice del dispositivo de audio predeterminado es 0.

-r <texto>
   Sets the audio output device by its name.

-c
   Toma como entrada el texto del portapapeles.

-t <texto>
   El texto de entrada se puede tomar de la l�nea de comandos.

-i
   Toma el texto de entrada de STDIN.

-o
   SAPI 4: la opci�n no se usa.
   SAPI 5 y Microsoft Speech Platform: escribe los datos sonoros en STDOUT. Si se especifica la opci�n, la opci�n [-w] se ignora.

-s <entero>
   SAPI 4: establece la velocidad de la voz en el rango de 0 a 100 (no hay valor predeterminado).
   SAPI 5 y Microsoft Speech Platform: configurar la velocidad de la voz en el rango de -10 a 10 (el valor predeterminado es 0).

-p <entero>
   SAPI 4: establece el tono de la voz en el rango de 0 a 100 (no hay valor predeterminado).
   SAPI 5 y Microsoft Speech Platform: establece el tono de la voz en el rango de -10 a 10 (el valor predeterminado es 0).

-v <entero>
   SAPI 4: la opci�n no se usa.
   SAPI 5 y Microsoft Speech Platform: establece el volumen en el rango de 0 a 100 (el valor predeterminado es 100).

-e <entero>
   Ajusta la longitud de las pausas entre frases (en milisegundos). El valor predeterminado es 0.

-a <entero>
   Ajusta la longitud de las pausas entre p�rrafos (en milisegundos). El valor predeterminado es 0.

-d <nombre_de_archivo>
   Usa un diccionario para la correcci�n de la pronunciaci�n (*.BXD, *.DIC o *.REX). La l�nea de comandos puede contener varias opciones [-d].

-k
   Elimina otras copias de la aplicaci�n de consola de la memoria del equipo.

-ka
   Elimina de la memoria del equipo la copia de la aplicaci�n de consola activa.

-pr
   Make pause or resume reading aloud by the active copy of the application. The action is the same as for the context menu item "Pause"/"Resume".

-q
   A�ade la aplicaci�n a una cola. La aplicaci�n de consola esperar� hasta que otras copias del programa finalicen.

-lrc
   SAPI 4: la opci�n no se usa.
   SAPI 5 y Microsoft Speech Platform: Si se especifican las opciones [-w] u [-o], crea el archivo LRC.

-srt
   SAPI 4: la opci�n no se usa.
   SAPI 5 y Microsoft Speech Platform: Si se especifican las opciones [-w] u [-o], crea el archivo SRT.

-vs <nombre_de_archivo>
   SAPI 4: la opci�n no se usa.
   SAPI 5 y Microsoft Speech Platform: sets the name of output text file with visemes, if the option [-w] is specified.
   A viseme is the mouth shape that corresponds to a particular speech sound. SAPI supports the list of 21 visemes.
   This list is based on the original Disney visemes. The application will create the audio file and then read it aloud to get visemes and their timecodes.
   The list of visemes supported by SAPI 5: https://msdn.microsoft.com/en-us/library/ms720881(v=vs.85).aspx

-sub
   SAPI 4: la opci�n no se usa.
   SAPI 5 y Microsoft Speech Platform: el texto se procesar� como subt�tulos. La opci�n puede ser �til al especificar las opciones [-i] o [-c].

-tray
   Muestra el icono del programa en la bandeja del sistema. Ello permite observar el progreso de la tarea.
   Puede utilizarse el elemento "Stop" del men� contextual para detener el proceso.

-ln <entero>
   Selecciona una l�nea del archivo de texto empleando un n�mero de l�nea. La numeraci�n de las l�neas empieza por "1".
   Para seleccionar m�s de una l�nea se puede emplear el intervalo de n�meros (por ejemplo, "26-34").
   La l�nea de comandos puede contener varias opciones [-ln].

-fr <entero>
   SAPI 4: la opci�n no se usa.
   SAPI 5 y Microsoft Speech Platform: sets the output audio sampling frequency in kHz (8, 11, 12, 16, 22, 24, 32, 44, 48).
   If the option is not specified, the default value of the selected voice will be used.

-bt <entero>
   SAPI 4: la opci�n no se usa.
   SAPI 5 y Microsoft Speech Platform: sets the output audio bit depth (8 o 16).
   If the option is not specified, the default value of the selected voice will be used.

-ch <entero>
   SAPI 4: la opci�n no se usa.
   SAPI 5 y Microsoft Speech Platform: sets the output audio channel mode (1 o 2).
   If the option is not specified, the default value of the selected voice will be used.

-? o -h
   Muestra la lista de opciones de l�nea de comandos disponibles.

--encoding <codificaci�n> o -enc <codificaci�n>
   Establece la codificaci�n del texto de entrada ("ansi", "utf8" o "unicode"). El valor predeterminado es "ansi".

--silence-begin <entero> o -sb <entero>
   Ajusta la longitud del silencio al principio del archivo de audio (en milisegundos). El valor predeterminado es 0.

--silence-end <entero> o -se <entero>
   Ajusta la longitud del silencio al final del archivo de audio (en milisegundos). El valor predeterminado es 0.

--lrc-length <entero>
   Ajusta la longitud m�xima de l�neas para el archivo LRC (en caracteres).

--lrc-fname <nombre_de_fichero>
   Establece el nombre del archivo LRC. La opci�n puede ser �til cuando se especifica la opci�n [-o].

--lrc-enc <codificaci�n>
   Establece la codificaci�n del archivo LRC ("ansi", "utf8" o "unicode"). El valor predeterminado es "ansi".

--lrc-offset <entero>
   Ajusta el desplazamiento del tiempo para el archivo LRC (en milisegundos).

--lrc-artist <texto>
   Establece la etiqueta de ID para el archivo LRC: int�rprete.

--lrc-album <texto>
   Establece la etiqueta de ID para el archivo LRC: �lbum.

--lrc-title <texto>
   Establece la etiqueta de ID para el archivo LRC: t�tulo.

--lrc-author <texto>
   Establece la etiqueta de ID para el archivo LRC: autor.

--lrc-creator <texto>
   Establece la etiqueta de ID para el archivo LRC: creador del archivo LRC.

--srt-length <entero>
   Ajusta la longitud m�xima de l�neas para el archivo SRT (en caracteres).

--srt-fname <nombre_de_fichero>
   Establece el nombre del archivo SRT. La opci�n puede ser �til cuando se especifica la opci�n [-o].

--srt-enc <codificaci�n>
   Establece la codificaci�n del archivo SRT ("ansi", "utf8" o "unicode"). El valor predeterminado es "ansi".

--raw
   SAPI 4: la opci�n no se usa.
   SAPI 5 y Microsoft Speech Platform: grabar los datos de audio en el formato RAW PCM; los datos no contienen el encabezado del formato WAV.
   La opci�n se utiliza junto con la opci�n [-o].

--ignore-length o -il
   SAPI 4: la opci�n no se usa.
   SAPI 5 y Microsoft Speech Platform: no grabar la dimensi�n de los datos de audio en el encabezado del formato WAV.
   La opci�n se utiliza junto con la opci�n [-o].

--sub-format <texto>
   Establece el formato de subt�tulos ("srt", "ssa", "ass" o "smi"). Si no se especifica la opci�n, el formato se definir� acorde a la extensi�n del archivo.

--sub-fit o -sf
   Aumenta autom�ticamente la velocidad del habla para poder ajustarse en los intervalos de tiempo, especificados en los subt�tulos.

--sub-max <entero> o -sm <entero>
   Ajusta la velocidad m�xima del habla en la gama de -10 a 10 (para convertir los subt�tulos en un fichero de audio).

--delete-file o -df
   Al terminar el trabajo, borra el archivo de texto. La opci�n se utiliza junto con la opci�n [-f].

--ignore-square-brackets o -isb
   Ignore text in [square brackets].

--ignore-curly-brackets o -icb
   Ignore text in {curly brackets}.

--ignore-angle-brackets o -iab
   Ignore text in <angle brackets>.

--ignore-round-brackets o -irb
   Ignore text in (round brackets).

--ignore-comments o -ic
   Omite los comentarios. Los comentarios de una sola l�nea comienzan con // y contin�an hasta el final de la l�nea. Los comentarios de varias l�neas comienzan con /* y terminan con */.


*** Ejemplos ***

balcon -l

balcon -n "Microsoft Anna" -m

balcon -f "d:\Texto\libro.txt" -w "d:\Sonido\libro.wav" -n "Emma"

balcon -n "Callie" -c -d "d:\rex\reglas.rex" -d "d:\dic\reglas.dic"

balcon -n "Conchita" -t "El texto ser� le�do lentamente." -s -5 -v 70

balcon -k

balcon -f "d:\Texto\libro.txt" -w "d:\Sonido\libro.wav" -lrc --lrc-length 80 --lrc-title "El Se�or de los Anillos"

balcon -f "d:\Texto\film.srt" -w "d:\Sonido\film.wav" -n "Laura" --sub-fit --sub-max 2


Ejemplo de utilizaci�n de la aplicaci�n junto con la utilidad LAME.EXE:

balcon -f d:\libro.txt -n Ines -o --raw | lame -r -s 22.05 -m m -h - d:\libro.mp3


Ejemplo de utilizaci�n de la aplicaci�n junto con la utilidad OGGENC2.EXE:

balcon -f d:\libro.txt -n Ines -o -il | oggenc2 --ignorelength - -o d:\libro.ogg


Ejemplo de utilizaci�n de la aplicaci�n junto con la utilidad WMAENCODE.EXE:

balcon -f d:\libro.txt -n Ines -o -il | wmaencode - d:\libro.wma --ignorelength


*** Archivo de configuraci�n ***

Se puede guardar el archivo de configuraci�n "balcon.cfg" en la misma carpeta que la aplicaci�n de consola.

Un ejemplo del contenido del archivo:
===============
-f d:\Texto\libro.txt
-w d:\Sonido\libro.wav
-n Microsoft Anna
-s 2
-p -1
-v 95
-e 300
-d d:\Dict\reglas.bxd
-lrc
--lrc-length 75
--lrc-enc utf8
--lrc-offset 300
===============

El programa puede combinar opciones del archivo de configuraci�n y de la l�nea de comandos.


*** Clips de audio ***

La aplicaci�n permite insertar v�nculos a archivos WAV externos (clips de audio) en el texto. La etiqueta de clip de audio ser� similar a esto:

{{Audio=C:\Sonidos\timbre.wav}}

Al leer el texto en voz alta, el programa se pausar� cuando llegue a la etiqueta del clip de audio, reproducir� el clip de audio y seguir� hablando.
Al convertir en archivos de audio, el clip de audio se incrustar� en el archivo de audio que cree la aplicaci�n.


*** Licencia ***

Derecho al uso no comercial del programa:
- para las personas naturales: sin ning�n tipo de restricciones;
- para las personas jur�dicas: sujeto a las restricciones que figuran en el "Contrato de licencia" del software Balabolka.

El uso comercial del programa s�lo se permite con previa autorizaci�n del titular de derechos de autor.

###