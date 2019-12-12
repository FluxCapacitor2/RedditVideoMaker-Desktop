Balabolka (Konsolenanwendung), Version 1.57
Copyright (c) 2013-2019 Ilya Morozov
Alle Rechte vorbehalten

WWW: http://balabolka.site/de/bconsole.htm
E-mail: crossa@list.ru (nur russisch oder englisch)

Lizenzart: Freeware
Plattformen: Microsoft Windows XP/Vista/7/8/10
Microsoft Speech API: 4.0/5.0 und h�her
Microsoft Speech Platform: 11.0



*** Anwendung ***

balcon [Optionen ...]


*** Befehlszeilen-Optionen ***

-l
   Druckt die Liste der verf�gbaren Stimmen.

-g
   Druckt die Liste der verf�gbaren Audio-Ausgabeger�te.

-f <Dateiname>
   Bestimmt den Namen der Eingabe-Textdatei.

-w <Dateiname>
   Bestimmt den Namen der Ausgabe-Datei im WAV-Format. Wenn diese Option angegeben ist, wird eine Audio-Datei erstellt. Sonst wird der Text laut vorgelesen.

-n <Sprecher>
   Bestimmt den Namen des Sprechers (ein Teil des Namens ist ausreichend). 
   Wenn diese Option nicht festgelegt wurde, wird die Stimme, die durch die Option [-id] bestimmt wurde, oder die Standard-Stimme von Windows verwendet.

-id <Zahl>
   Setzt die "Lokale ID" f�r die Stimme. Die Lokale ID ist der von Microsoft zugewiesene Sprachen-Code
   (z. B. "1031" oder "0x0407" f�r "Deutsch - Deutschland", "1033" oder "0x0409" f�r "Englisch - Amerika").
   Das Programm wird die erste Stimme mit definierter Lokaler ID aus der Liste von Stimmen ausw�hlen. 
   Wenn diese Option nicht festgelegt wurde, wird die Stimme, die durch die Option [-n] bestimmt wurde, oder die Standard-Stimme von Windows verwendet.
   Liste der Sprachen mit zugewiesenen Codes: https://msdn.microsoft.com/en-us/library/cc233982.aspx

-m
   Druckt die Parameter der Stimme.

-b <Zahl>
   W�hlt das Audio-Ausgabeger�t nach seinem Index. Der Index des Standard-Audio-Ausgabeger�tes ist 0.

-r <Textzeile>
   W�hlt das Audio-Ausgabeger�t nach seinem Namen.

-c
   Verwendet die Texteingabe aus der Zwischenablage.

-t <Textzeile>
   Die Texteingabe kann von der Befehlszeile verwendet werden.

-i
   Verwendet die Texteingabe von STDIN.

-o
   SAPI 4: nicht verwendet.
   SAPI 5 und Microsoft Speech Platform: Schreibt Tondaten auf STDOUT. Wenn diese Option gew�hlt ist, wird die Option [-w] ignoriert.

-s <Zahl>
   SAPI 4: Bestimmt die Geschwindigkeit in einem Bereich von 0 bis 100 (kein Standardwert).
   SAPI 5 und Microsoft Speech Platform: Bestimmt die Geschwindigkeit in einem Bereich von -10 bis 10 (der Standardwert ist 0).

-p <Zahl>
   SAPI 4: Bestimmt die Tonh�he in einem Bereich von 0 bis 100 (kein Standardwert).
   SAPI 5 und Microsoft Speech Platform: Bestimmt die Tonh�he in einem Bereich von -10 bis 10 (der Standardwert ist 0).

-v <Zahl>
   SAPI 4: nicht verwendet.
   SAPI 5 und Microsoft Speech Platform: Bestimmt das Volumen in einem Bereich von 0 bis 100 (der Standardwert ist 100).

-e <Zahl>
   Bestimmt die L�nge der Pausen zwischen den S�tzen (in Millisekunden). Der Standardwert ist 0.

-a <Zahl>
   Bestimmt die L�nge der Pausen zwischen den Abs�tzen (in Millisekunden). Der Standardwert ist 0.

-d <Dateiname>
   Verwendet das W�rterbuch f�r die Aussprache-Korrektur (*.BXD, *.DIC oder *.REX). Die Befehlszeile kann ein paar Optionen enthalten [-d].

-k
   Beendet weitere Exemplare der Konsolen-Anwendung im Arbeitsspeicher des Computers.

-ka
   Beendet das aktive Exemplar der Konsolen-Anwendung im Arbeitsspeicher des Computers.

-pr
   Macht eine Pause oder beginnt mit dem Lesen der aktiven Kopie der Anwendung. Die Aktion ist dieselbe wie f�r den Kontextmen�eintrag "Pause"/"Fortsetzen".

-q
   Setzt die Anwendung in eine Warteschlange. Die Konsolen-Anwendung wartet, bis andere Exemplare des Programms fertig sind.

-lrc
   SAPI 4: nicht verwendet.
   SAPI 5 und Microsoft Speech Platform: Erstellt eine LRC-Datei, wenn die Option [-w] oder [-o] angegeben ist.

-srt
   SAPI 4: nicht verwendet.
   SAPI 5 und Microsoft Speech Platform: Erstellt eine SRT-Datei, wenn die Option [-w] oder [-o] angegeben ist.

-vs <Dateiname>
   SAPI 4: nicht verwendet.
   SAPI 5 und Microsoft Speech Platform: Bestimmt den Namen der ausgegebenen Textdatei mit Visemen, wenn die Option [-w] angegeben ist.
   Ein Visem ist die Mundform, die einem bestimmten Sprachklang entspricht. SAPI unterst�tzt die Liste von 21 Visemen.
   Diese Liste basiert auf den originalen Disney Visemen. Die Anwendung erstellt die Audiodatei und liest sie dann laut, um Viseme und ihre Timecodes zu erhalten.
   Die Liste der von SAPI 5 unterst�tzten Viseme: https://msdn.microsoft.com/en-us/library/ms720881(v=vs.85).aspx

-sub
   SAPI 4: nicht verwendet.
   SAPI 5 und Microsoft Speech Platform: Text wird als Untertitel verarbeitet. Diese Option kann n�tzlich sein, wenn die Optionen [-i] oder [-c] festgelegt wurden.

-tray
   Das Programm-Icon im System-Tray anzeigen. Dies erm�glicht es, den Fortschritt der Aufgabe zu sehen.
   Mit der Option "Stopp" im Kontext-Men� kann man den Prozess stoppen.

-ln <Zahl>
   W�hlt eine Zeile aus einer Textdatei mit Hilfe einer Zeilennummer. Die Zeilennummerierung beginnt bei "1".
   Ein Intervall von Zahlen kann verwendet werden, um mehr als eine Zeile auszuw�hlen (beispielsweise "26-34").
   Die Befehlszeile kann ein paar Optionen enthalten [-ln].

-fr <Zahl>
   SAPI 4: nicht verwendet.
   SAPI 5 und Microsoft Speech Platform: Legt die Ausgabe-Audio-Abtastfrequenz in kHz fest (8, 11, 12, 16, 22, 24, 32, 44, 48).
   Wenn die Option nicht angegeben ist, wird der Standardwert f�r die ausgew�hlte Stimme verwendet.

-bt <Zahl>
   SAPI 4: nicht verwendet.
   SAPI 5 und Microsoft Speech Platform: Legt die Audio-Bittiefe des Ausgangs (8 oder 16) fest.
   Wenn die Option nicht angegeben ist, wird der Standardwert f�r die ausgew�hlte Stimme verwendet.

-ch <Zahl>
   SAPI 4: nicht verwendet.
   SAPI 5 und Microsoft Speech Platform: Legt den Ausgabe-Audiokanal-Modus (1 oder 2) fest.
   Wenn die Option nicht angegeben ist, wird der Standardwert f�r die ausgew�hlte Stimme verwendet.

-? oder -h
   Druckt die Liste der verf�gbaren Befehlszeilen-Optionen.

--encoding <Kodierung> oder -enc <Kodierung>
   Bestimmt die Kodierung f�r die Texteingabe ("ansi", "utf8" oder "unicode"). Die Standardeinstellung ist "ansi".

--silence-begin <Zahl> oder -sb <Zahl>
   Legt die L�nge der Stille am Anfang der Audio-Datei fest (in Millisekunden). Der Standardwert ist 0.

--silence-end <Zahl> oder -se <Zahl>
   Legt die L�nge der Stille am Ende der Audio-Datei fest (in Millisekunden). Der Standardwert ist 0.

--lrc-length <Zahl>
   Bestimmt die maximale L�nge der Textzeilen f�r die LRC-Datei (in Zeichen).

--lrc-fname <Dateiname>
   Bestimmt den Namen der LRC-Datei. Die Option kann n�tzlich sein, wenn die Option [-o] angegeben ist.

--lrc-enc <Kodierung>
   Bestimmt die Kodierung f�r die LRC-Datei ("ansi", "utf8" oder "unicode"). Die Standardeinstellung ist "ansi".

--lrc-offset <Zahl>
   Bestimmt die Zeitverschiebung f�r die LRC-Datei (in Millisekunden).

--lrc-artist <Textzeile>
   Bestimmt den ID-Tag f�r die LRC-Datei: K�nstler.

--lrc-album <Textzeile>
   Bestimmt den ID-Tag f�r die LRC-Datei: Album.

--lrc-title <Textzeile>
   Bestimmt den ID-Tag f�r die LRC-Datei: Titel.

--lrc-author <Textzeile>
   Bestimmt den ID-Tag f�r die LRC-Datei: Autor.

--lrc-creator <Textzeile>
   Bestimmt den ID-Tag f�r die LRC-Datei: Ersteller der LRC-Datei.

--srt-length <Zahl>
   Bestimmt die maximale L�nge der Textzeilen f�r die SRT-Datei (in Zeichen).

--srt-fname <Dateiname>
   Bestimmt den Namen der SRT-Datei. Die Option kann n�tzlich sein, wenn die Option [-o] angegeben ist.

--srt-enc <Kodierung>
   Bestimmt die Kodierung f�r die SRT-Datei ("ansi", "utf8" oder "unicode"). Die Standardeinstellung ist "ansi".

--raw
   SAPI 4: nicht verwendet.
   SAPI 5 und Microsoft Speech Platform: Der Ausgang ist RAW-Format PCM; die Audiodaten enthalten nicht den WAV-Header.
   Diese Option wird zusammen mit der Option [-o] verwendet.

--ignore-length oder -il
   SAPI 4: nicht verwendet.
   SAPI 5 und Microsoft Speech Platform: Ignoriert die L�nge der Daten im WAV-Header.
   Diese Option wird zusammen mit der Option [-o] verwendet.

--sub-format <Textzeile>
   Legt das Format der Untertitel fest ("srt", "ssa", "ass" oder "smi"). Wenn diese Option nicht angegeben ist, wird das Format anhand der Dateiendung bestimmt.

--sub-fit oder -sf
   Erh�ht die Geschwindigkeit automatisch, passend zu Zeitintervallen (wenn das Programm Untertitel in eine Audio-Datei konvertiert).

--sub-max <Zahl> oder -sm <Zahl>
   Legt die maximale Sprechgeschwindigkeit in einem Bereich von -10 bis 10 fest (wenn das Programm Untertitel in eine Audio-Datei konvertiert).

--delete-file oder -df
   Entfernt die Textdatei, wenn die Aufgabe erledigt ist. Diese Option wird zusammen mit der Option [-f] verwendet.

--ignore-square-brackets oder -isb
   Ignore text in [square brackets].

--ignore-curly-brackets oder -icb
   Ignore text in {curly brackets}.

--ignore-angle-brackets oder -iab
   Ignore text in <angle brackets>.

--ignore-round-brackets oder -irb
   Ignore text in (round brackets).

--ignore-comments oder -ic
   Ignore comments in text. Single-line comments start with // and continue until the end of the line. Multiline comments start with /* and end with */.


*** Beispiele ***

balcon -l

balcon -n "Microsoft Anna" -m

balcon -f "d:\Text\book.txt" -w "d:\Sound\book.wav" -n "Emma"

balcon -n "Callie" -c -d "d:\rex\rules.rex" -d "d:\dic\rules.dic"

balcon -n "Matthias" -t "Der Text wird langsam vorgelesen." -s -5 -v 70

balcon -k

balcon -f "d:\Text\book.txt" -w "d:\Sound\book.wav" -lrc --lrc-length 80 --lrc-title "The Lord of the Rings"

balcon -f "d:\Text\film.srt" -w "d:\Sound\film.wav" -n "Laura" --sub-fit --sub-max 2


Beispiel f�r die Verwendung zusammen mit LAME.EXE:

balcon -f d:\book.txt -n Klaus -o --raw | lame -r -s 22.05 -m m -h - d:\book.mp3


Beispiel f�r die Verwendung zusammen mit OGGENC2.EXE:

balcon -f d:\book.txt -n Klaus -o -il | oggenc2 --ignorelength - -o d:\book.ogg


Beispiel f�r die Verwendung zusammen mit WMAENCODE.EXE:

balcon -f d:\book.txt -n Klaus -o -il | wmaencode - d:\book.wma --ignorelength


*** Konfigurationsdatei ***

Die Befehlszeilen-Optionen k�nnen als Konfigurationsdatei "balcon.cfg" im Ordner der Konsolen-Anwendung gespeichert werden. 

Beispiel f�r eine Konfigurationsdatei:
===============
-f d:\Text\book.txt
-w d:\Sound\book.wav
-n Microsoft Anna
-s 2
-p -1
-v 95
-e 300
-d d:\Dict\rules.bxd
-lrc
--lrc-length 75
--lrc-enc utf8
--lrc-offset 300
===============

Das Programm kann Optionen von der Konfigurationsdatei und der Kommandozeile kombinieren.


*** Audio-Clips ***

Das Programm erm�glicht, Links zu externen WAV-Dateien (Audio-Clips) in Text von Dokumenten einzuf�gen. Ein Audio-Clip-Tag wird so aussehen:

{{Audio=C:\Sounds\ring.wav}}

Beim Vorlesen von Text wird Balabolka unterbrochen, wenn das Audio-Clip-Tag erreicht ist, der Audio-Clip wird gespielt und das Vorlesen wird fortgesetzt.
Bei der Umwandlung in Audio-Dateien wird der Audio-Clip in der Audio-Datei, die durch das Programm erstellt wird, eingebettet werden.


*** Lizenzart ***

Sie k�nnen Software f�r nichtkommerzielle Zwecke verwenden und vertreiben. F�r die kommerzielle Nutzung oder den Vertrieb ben�tigen Sie die Genehmigung des Urheberrechtsinhabers.

###