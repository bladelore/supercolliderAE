s.boot;

Quarks.install("TimeStretch");


x = {SinOsc.ar([200,201]) * 0.1}.play;

x.free;
s.freeAll;

//JITlib

n=NodeProxy.new;

n.isPlaying;

n.play;
//BIG CLEAR
//fade in seconds
n.clear(2);

n.source;


//set fade time between sources
n.fadeTime_(3);
n.source = { PinkNoise.ar(0.1!2) };
n.source = { SinOsc.ar([200,201]) * 0.1 };

//blahblah

Ndef(\key, {SinOsc.ar(700)}).play

Ndef(\first, {SinOsc.ar(400!2) * 0.1});
Ndef(\first).play;
Ndef(\first).stop;
Ndef(\first, {|freq=500| SinOsc.ar(freq) * 0.1});
Ndef(\first).set(\freq,300);

//start time
Ndef(\first).play(fadeTime:5);
//CROSSFADE time
Ndef(\first).fadeTime=3;
Ndef(\first, {Saw.ar(400,0.2)});
Ndef(\first).stop(fadeTime:5);

Ndef(\second, {SinOsc.ar(500, 0, 0.5)})
Ndef(\second).play
Ndef(\second).stop
Ndef(\second, {Pulse.ar(600, 0.5, 0.5)})
Ndef(\second).play
Ndef(\second).stop

//NEST NDEFS
Ndef(\third, {LPF.ar(Ndef.ar(\second))})
Ndef(\third).play
Ndef(\third, {BPF.ar(Ndef.ar(\second), 500, 0.1)})
Ndef(\third).stop


//ndef defaults to stereo
Ndef(\k).play(0, 3);
Ndef(\k, {SinOsc.ar([400,500,600])});
Ndef(\k).numChannels;
//clear all ndefs
Ndef.clear;

//ndef as args
(
Ndef(\padFreqs, {[404, 505, 606, 707]+LFNoise2.ar(0.1!4, 30)});
Ndef(\padAmps, {[0.4, 0.3, 0.2, 0.5]+LFNoise2.ar(0.1!4, 0.1)});
Ndef(\padCut, 2000);
Ndef(\padQ, 0.8);
Ndef(\pad, {
	Splay.ar(
		BLowPass4.ar(
			Saw.ar(
				Ndef.ar(\padFreqs),
				Ndef.ar(\padAmps)
			),
			Ndef.ar(\padCut),//cutoff freq for filter
			Ndef.ar(\padQ)//q for filter
		)
	)
}).play(fadeTime:10);
)

Ndef.clear;

//mads tutorial
//https://www.youtube.com/watch?v=PJdyTTZERYs
(
Ndef(\q3, {|freq=444, pan=0|
	var sig = LFTri.ar(freq);
	sig=Pan2.ar(sig,pan);
	sig * 0.45;
}).play;
)

Ndef(\q3).fadeTime = 1;

Ndef(\q3).set(\freq,50,\pan,0);

//fadetime works for xset (xset....crossfadeset)
Ndef(\q3).xset(\freq, rrand(80,500),\pan, rrand(-1,1));

~fqshift = {|in, freqshiftfreq=1| FreqShift.ar(in, freqshiftfreq)}

//add fx
(
//\filter is hardcoded key - not the custom name
Ndef(\q3)[1] = \filter -> { |in| PitchShift.ar(in, 0.25, \pitch.kr(0.5), \pitchdispersion.kr(0.01), \timedispersion.kr(0.5))}
)

//drywet control is wet+slot number (hardcoded)
Ndef(\q3).xset(\wet1, 0.5);

//free gui wtf
Ndef(\q3).gui

//copy ndef
Ndef(\q3).copy(\q4);
Ndef(\q4).xset(\freq, rrand(80, 100), \pan, rrand(-1,1)).play;
Ndef(\q3).xset(\freq,200,\wet1,0.9);

Ndef(\q4).stop;
///pattern control
//idx 999 to leave room for fx
//pset normal, xset use crossfade

(
Pdef(\modpatt, Pbind(
	\dur, Pseq([1,0.5,0.3,0.2], inf),
	\freq, Pwhite(100, 500),
	\pan, Pwhite(-1,1),
	\timedispersion, 0.5,
	\pitchdispersion, 0.5,
));
)

(

Ndef(\q3)[999] = \xset -> Pdef(\modpatt);

)

Ndef(\q4).play;

//create lfo
Ndef(\lfo, { LFSaw.kr(\lfofreq.kr(0.1)) });
//map lfo pattern
Ndef(\lfo)[999] = \xset -> Pbind(\dur, Pwhite(1.0,8.0), \lfofreq, Pwhite(0.001, 10));
//define second lfo
Ndef(\lfo2, { LFSaw.kr(\lfofreq.kr(10)).linlin(-1,1,40,2500) });
//map lfo 2 speed to lfo 1
Ndef(\lfo2).map(\lfofreq, Ndef(\lfo));
//map lfos to params;
Ndef(\q4).map(\pitchdispersion, Ndef(\lfo), \freq, Ndef(\lfo2));
//copy to third lfo
Ndef(\lfo).copy(\lfo3);
Ndef(\q4).xmap(\pan, Ndef(\lfo3));

(
Pdef(\example,
    Pbind(
        \instrument, \default,
        \freq, Pfunc({Ndef(\d, { XLine.ar(2000, 20, 0.25) + 200; }).asMap}),
        \dur, 0.5, \amp, 0.1,
    )
).play;
);