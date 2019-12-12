Balabolka (aplicativo de console), vers�o 1.57
Copyright (c) 2013-2019 Ilya Morozov
All Rights Reserved

WWW: http://balabolka.site/br/bconsole.htm
E-mail: crossa@list.ru

Licen�a: Freeware
Sistema operacional: Microsoft Windows XP/Vista/7/8/10
Microsoft Speech API: 4.0/5.0
Microsoft Speech Platform: 11.0



*** Linha de comando ***

balcon [op��es ...]


*** Op��es de comando ***

-l
   Mostrar a lista das vozes instaladas no seu computador.

-g
   Mostrar a lista dos dispositivos de �udio dispon�veis.

-f <nome_do_arquivo>
   Abrir o arquivo de texto.

-w <nome_do_arquivo>
   Gravar um arquivo de �udio em formato WAV. Especificando a op��o, ser� criado o arquivo de �udio. Se a op��o n�o � especificada, o texto ser� lido em voz alta.

-n <nome_da_voz>
   Nome de voz (basta digitar uma parte do nome). No caso de n�o ser especificada, ser� usada a voz selecionada no painel de controlo do Windows.

-id <n�mero>
   Selecione a voz artificial usando o identificador de l�ngua (Locale ID). Esses identificadores est�o atribu�dos a todas as l�nguas pelos desenvolvedores da Microsoft
   (por exemplo, "1033" ou "0x0409" para ingl�s, "1046" ou "0x0416" para o portugu�s brasileiro).
   O programa vai selecionar a partir de uma lista a primeira voz, cujo identificador de l�ngua corresponde a um valor especificado.
   Se a op��o n�o for especificada, ser� usada a voz indicada pelo par�metro [-n], ou a voz selecionada no Painel de Controlo do Windows.
   Lista de identificadores: https://msdn.microsoft.com/en-us/library/cc233982.aspx

-m
   Mostrar par�metros para a voz selecionada.

-b <n�mero>
   Escolher o dispositivo de �udio segundo o n�mero na lista de dispositivos dispon�veis para reprodu��o de �udio. O n�mero padr�o do dispositivo � 0.

-r <texto>
   Sets the audio output device by its name.

-c
   Usar o texto a partir da �rea de transfer�ncia.

-t <linha_de_texto>
   Usar o texto a partir da linha de comando.

-i
   Usar o texto a partir do fluxo de entrada padr�o (STDIN).

-o
   SAPI 4: a op��o n�o � usada.
   SAPI 5 e Microsoft Speech Platform: gravar dados de �udio para o fluxo de sa�da padr�o (STDOUT); em caso de ser especificado, a op��o [-w] � ignorada.

-s <n�mero>
   SAPI 4: configurar a velocidade de fala na faixa de 0 a 100 (sem padr�o).
   SAPI 5 e Microsoft Speech Platform: configurar velocidade de fala na faixa de -10 a 10 (o padr�o � 0).

-p <n�mero>
   SAPI 4: ajustar o tom da voz, na faixa de 0 a 100 (sem padr�o).
   SAPI 5 e Microsoft Speech Platform: definir o tom de voz na faixa de -10 a 10 (o padr�o � 0).

-v <n�mero>
   SAPI 4: a op��o n�o � usada.
   SAPI 5 e Microsoft Speech Platform: definir o volume na faixa de 0 a 100 (o padr�o � 100).

-e <n�mero>
   Ajustar o comprimento das pausas entre as frases (em milissegundos). O padr�o � 0.

-a <n�mero>
   Ajustar o comprimento das pausas entre os par�grafos (em milissegundos). O padr�o � 0.

-d <nome_do_arquivo>
   Use o dicion�rio para a pron�ncia correcta (arquivo com nextens�o *.BXD, *.DIC ou *.REX). A linha de comando pode conter v�rias op��es [-d].

-k
   Parar o funcionamento de outras c�pias do aplicativo de console em execu��o no computador.

-ka
   Parar o funcionamento de um aplicativo de console que est� activo no momento.

-pr
   Make pause or resume reading aloud by the active copy of the application. The action is the same as for the context menu item "Pause"/"Resume".

-q
   Adicionar o aplicativo para a fila. O aplicativo de console vai esperar at� que as outras c�pias do programa terminem de funcionar.

-lrc
   SAPI 4: a op��o n�o � usada.
   SAPI 5 e Microsoft Speech Platform: criar um ficheiro do formato LRC, estando especificados os par�metros [-w] ou [-o].

-srt
   SAPI 4: a op��o n�o � usada.
   SAPI 5 e Microsoft Speech Platform: criar um ficheiro do formato SRT, estando especificados os par�metros [-w] ou [-o].

-vs <nome_do_arquivo>
   SAPI 4: a op��o n�o � usada.
   SAPI 5 e Microsoft Speech Platform: sets the name of output text file with visemes, if the option [-w] is specified.
   A viseme is the mouth shape that corresponds to a particular speech sound. SAPI supports the list of 21 visemes.
   This list is based on the original Disney visemes. The application will create the audio file and then read it aloud to get visemes and their timecodes.
   The list of visemes supported by SAPI 5: https://msdn.microsoft.com/en-us/library/ms720881(v=vs.85).aspx

-sub
   SAPI 4: a op��o n�o � usada.
   SAPI 5 e Microsoft Speech Platform: o texto representa subt�tulos e deve ser convertido em um ficheiro �udio com os intervalos de tempo especificados.

-tray
   Mostrar o �cone do programa na �rea de notifica��o do sistema operacional.
   Isso permite que o usu�rio monitore o progresso da opera��o, bem como interromper o processo no menu de contexto, clicando em "Stop".

-ln <n�mero>
   Selects a line from the text file by using of a line number. The line numbering starts at "1".
   The interval of numbers can be used for selecting of more than one line (for example, "26-34").
   A linha de comando pode conter v�rias op��es [-ln].

-fr <n�mero>
   SAPI 4: a op��o n�o � usada.
   SAPI 5 e Microsoft Speech Platform: sets the output audio sampling frequency in kHz (8, 11, 12, 16, 22, 24, 32, 44, 48).
   If the option is not specified, the default value of the selected voice will be used.

-bt <n�mero>
   SAPI 4: a op��o n�o � usada.
   SAPI 5 e Microsoft Speech Platform: sets the output audio bit depth (8 ou 16).
   If the option is not specified, the default value of the selected voice will be used.

-ch <n�mero>
   SAPI 4: a op��o n�o � usada.
   SAPI 5 e Microsoft Speech Platform: sets the output audio channel mode (1 ou 2).
   If the option is not specified, the default value of the selected voice will be used.

-? ou -h
   Mostrar a descri��o das op��es da linha de comando.

--encoding <codifica��o> ou -enc <codifica��o>
   Codifica��o de texto a partir da entrada padr�o ("ansi", "utf8" ou "unicode"). O valor padr�o � "ansi".

--silence-begin <n�mero> ou -sb <n�mero>
   Especificar a dura��o da pausa no in�cio do ficheiro �udio (em milissegundos). O padr�o � 0.

--silence-end <n�mero> ou -se <n�mero>
   Especificar a dura��o da pausa no final de um ficheiro �udio (em milissegundos). O padr�o � 0.

--lrc-length <n�mero>
   Especificar o comprimento m�ximo de cordas para ficheiro de formato LRC (em caracteres).

--lrc-fname <nome_do_arquivo>
   Nome  de ficheiro  do formato  LRC. A op��o pode ser �til em casos em que a linha de comando estiver definido o par�metro [-o].

--lrc-enc <codifica��o>
   Codifica��o de ficheiro do formato LRC ("ansi", "utf8" ou "unicode"). O valor padr�o � "ansi".

--lrc-offset <n�mero>
   Especificar a mudan�a da hora no ficheiro do formato LRC (em milissegundos).

--lrc-artist <texto>
   Etiqueta para ficheiro do formato LRC: int�rprete da obra.

--lrc-album <texto>
   Etiqueta para ficheiro do formato LRC: �lbum.

--lrc-title <texto>
   Etiqueta para ficheiro do formato LRC: t�tulo da obra.

--lrc-author <texto>
   Etiqueta para ficheiro do formato LRC: autor.

--lrc-creator <texto>
   Etiqueta para ficheiro do formato LRC: criador do ficheiro.

--srt-length <n�mero>
   Especificar o comprimento m�ximo de cordas para ficheiro de formato SRT (em caracteres).

--srt-fname <nome_do_arquivo>
   Nome  de ficheiro  do formato SRT. A op��o pode ser �til em casos em que a linha de comando estiver definido o par�metro [-o].

--srt-enc <codifica��o>
   Codifica��o de ficheiro do formato SRT ("ansi", "utf8" ou "unicode"). O valor padr�o � "ansi".

--raw
   SAPI 4: a op��o n�o � usada.
   SAPI 5 e Microsoft Speech Platform: gravar �udio no formato PCM RAW;  os dados n�o cont�m t�tulo do formato WAV.
   Esta op��o � utilizada em conjunto com [-o].

--ignore-length ou -il
   SAPI 4: a op��o n�o � usada.
   SAPI 5 e Microsoft Speech Platform: n�o inscrever o tamanho dos dados de �udio no t�tulo do formato WAV.
   Esta op��o � utilizada em conjunto com [-o].

--sub-format <texto>
   Formato  dos subt�tulos ("srt", "ssa", "ass" ou "smi"). Se n�o for especificado, o formato ser� determinado pela extens�o do nome do ficheiro de subt�tulos.

--sub-fit ou -sf
   Aumentar automaticamente a velocidade da fala, a fim de conseguir cumprir os intervalos definidos nos subt�tulos.

--sub-max <n�mero> ou -sm <n�mero>
   Especificar a velocidade m�xima de fala na faixa de -10 a 10 (para converter subt�tulos em ficheiros de �udio).

--delete-file ou -df
   Eliminar um ficheiro de texto depois de ler em voz alta ou salvar o ficheiro de �udio.

--ignore-square-brackets ou -isb
   Ignore text in [square brackets].

--ignore-curly-brackets ou -icb
   Ignore text in {curly brackets}.

--ignore-angle-brackets ou -iab
   Ignore text in <angle brackets>.

--ignore-round-brackets ou -irb
   Ignore text in (round brackets).

--ignore-comments ou -ic
   Ignore comments in text. Single-line comments start with // and continue until the end of the line. Multiline comments start with /* and end with */.


*** Exemplos ***

balcon -l

balcon -n "Microsoft Anna" -m

balcon -f "d:\Text\book.txt" -w "d:\Sound\book.wav" -n "Emma"

balcon -n "Callie" -c -d "d:\rex\rules.rex" -d "d:\dic\rules.dic"

balcon -n "Ricardo" -t "O texto vai ser lido devagar." -s -5 -v 70

balcon -k

balcon -f "d:\Text\book.txt" -w "d:\Sound\book.wav" -lrc --lrc-length 80 --lrc-title "The Lord of the Rings"

balcon -f "d:\Text\film.srt" -w "d:\Sound\film.wav" -n "Laura" --sub-fit --sub-max 2


Exemplo de uso do aplicativo em conjunto com o programa utilit�rio LAME.EXE:

balcon -f d:\book.txt -n Celia -o --raw | lame -r -s 22.05 -m m -h - d:\book.mp3


Exemplo de uso do aplicativo em conjunto com o programa utilit�rio OGGENC2.EXE:

balcon -f d:\book.txt -n Celia -o -il | oggenc2 --ignorelength - -o d:\book.ogg


Exemplo de uso do aplicativo em conjunto com o programa utilit�rio WMAENCODE.EXE:

balcon -f d:\book.txt -n Celia -o -il | wmaencode - d:\book.wma --ignorelength


*** Arquivo de configura��o ***

� poss�vel salvar o arquivo de configura��o "balcon.cfg" na mesma pasta que o aplicativo de console.

Um exemplo do conte�do do arquivo:
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

O programa pode combinar op��es do arquivo de configura��o e da linha de comando.


*** Audio Clips ***

The application allows to insert links to external WAV files (audio clips) into text. Audio clip tag will look like:

{{Audio=C:\Sounds\ring.wav}}

When speaking text aloud, the program will pause when the audio clip tag is reached, play the audio clip and resume speaking.
When converting to audio files, the audio clip will be embedded in the audio file created by the application.


*** Licen�a ***

Direito ao uso n�o comercial do programa: 
- para pessoas singulares: sem restri��es; 
- para pessoas jur�dicas: sujeito �s restri��es contidas no "Contrato de licen�a" do software Balabolka. 

O uso comercial do programa s� � permitido com a autoriza��o pr�via do detentor dos direitos autorais.

###