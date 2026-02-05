(

    Ndef(\xyz,{
        var freq = \freq.kr(440);
        SinOsc.ar([freq, freq + 4]) * 0.1;
    });

    Ndef(\lfo1, { SinOsc.ar(t.tempo / \rate.kr(0.5), pi).linlin(-1, 1, 220, 440) });
    Ndef(\lfo1)[999] = \pset -> Pbind(\rate, ~knob.(0).linlin(0, 1, 1, 32).ceil.reciprocal * 8, \dur, 0.01);
    
    Ndef(\xyz).map(
        \freq, Ndef(\lfo1),
    );

    Ndef(\xyz).play(~bus1);
)