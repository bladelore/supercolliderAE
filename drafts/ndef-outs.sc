
//fade

Ndef(\test).clear;

Ndef(\test, { SinOsc.ar(\freq.kr(440)) * 0.1 ! 2;});

Ndef(\test).fadeTime = 10;

Ndef(\test).play;

Ndef(\test).xset(\freq, 220);

//no fade
(
    SynthDef(\my_synthdef, {
        var sig = SinOsc.ar(\freq.kr(440)) * 0.1;
        ReplaceOut.ar(\out.kr(0), sig ! 2);
    }).add;
)

Ndef(\test).clear;

Ndef(\test, \my_synthdef).fadeTime = 10;

Ndef(\test).play;

Ndef(\test).xset(\freq, 220);