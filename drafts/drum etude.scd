s.boot;

(
SynthDef(\kick, {
	var snd, partialDecays;
	//sine osc * freq sweep
	//freq sweep converges to base freq * XLine for extra freq sweep down
	//multichannel expand array
	snd = SinOsc.ar(\freq.kr(55) * (1 + (\sweep.kr(2) * Env.perc(0.0, 0.13, curve: -4).ar)) * XLine.ar(1, 0.6 ,1) * [1, 1.3, 5.4, 4.8]);
	//set db of each
	snd = snd * [0, -10, -5, -8].dbamp;
	//set env of each
	partialDecays = (3..0).lincurve(0, 3, 0.01, 3, 5);
	snd = snd * Env.perc(0, partialDecays * \partials.kr(1)).ar;
	//sum sines
	snd = snd.sum;
	//add second sine wave * second envelope
	snd = snd + (SinOsc.ar(XLine.ar(4000, 50, 0.01)) * Env.perc(0.0001, 0.01).ar * -5.dbamp);
	//add filtered noise
	snd = snd + (BPF.ar(WhiteNoise.ar, 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	//alternative using deterministic hash function (same seed)
	snd = snd + (BPF.ar(Hasher.ar(Sweep.ar), 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	//rectify and clip
	//modulate input gain to distortion
	snd = snd * (1 + (4 * Env.perc(0.01, 0.2).ar));
	//snd = snd + (GVerb.ar(snd, \roomsize.kr(8), \reverbtime.kr(1), spread: 16) * -30.dbamp);
	snd = ((snd * 10.dbamp)).tanh;
	//times envelope and free self on end
//	snd = snd * Env.perc(0.001, \dec.kr(2), curve: -3).ar(Done.freeSelf, \gate.kr(1));
	snd = snd * Env.perc(0.001, \dec.kr(2), curve: -3).ar(Done.freeSelf);
	//amplitude @ -10 db
	snd = snd * \gain.kr(-15).dbamp;
	//pan parameter and duplicate
	snd = Pan2.ar(snd, \pan.kr(0));
	//out parameter and set
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\kick2, {
	var snd;
	//sine osc * freq sweep
	//freq sweep converges to base freq * XLine for extra freq sweep down
	//multichannel expand array
	snd = SinOsc.ar(\freq.kr(55) * (1 + (\sweep.kr(2) * Env.perc(0.0, 0.13, curve: -4).ar)) * XLine.ar(1, 0.6 ,1));
	//sum sines
	//add second sine wave * second envelope
	snd = snd + (SinOsc.ar(XLine.ar(4000, 50, 0.01)) * Env.perc(0.0001, 0.01).ar * -5.dbamp);
	//add filtered noise
	snd = snd + (BPF.ar(WhiteNoise.ar, 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	//alternative using deterministic hash function (same seed)
	snd = snd + (BPF.ar(Hasher.ar(Sweep.ar), 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	//rectify and clip
	//modulate input gain to distortion
	snd = snd * (1 + (4 * Env.perc(0.01, 0.2).ar));
	//snd = snd + (GVerb.ar(snd, \roomsize.kr(8), \reverbtime.kr(1), spread: 16) * -30.dbamp);
	snd = ((snd * 10.dbamp)).tanh;
	//times envelope and free self on end
//	snd = snd * Env.perc(0.001, \dec.kr(2), curve: -3).ar(Done.freeSelf, \gate.kr(1));
	snd = snd * Env.perc(0.001, \dec.kr(2), curve: -3).ar(Done.freeSelf);
	//amplitude @ -10 db
	snd = snd * \gain.kr(-15).dbamp;
	//pan parameter and duplicate
	snd = Pan2.ar(snd, \pan.kr(0));
	//out parameter and set
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\snare, {
	var snd, noiseArr;
	snd = SinOsc.ar(\freq.kr(200) * (1 + (2 * Env.perc(0.001, 0.03).ar)) * ([1, 1.3, 2.5, 5.8]));
	snd = snd * Env.perc(0, [2, 0.4, 0.2, 0.1]).ar;
	snd = snd * [0, -20, -12, -24].dbamp;
	snd = snd.sum;
	snd = snd + (SinOsc.ar(XLine.ar(20000, 100, 0.03)) * Env.perc(0.01, 0.03).ar * -3.dbamp);
	snd = snd + (BPF.ar(Hasher.ar(Sweep.ar), 2120, 0.3) * Env.perc(0.05, 0.1).ar * -3.dbamp);
	snd = snd + (BPF.ar(Hasher.ar(Sweep.ar), 4120, 0.6) * Env.perc(0.003, 0.15).ar * -8.dbamp);
	snd = snd * Env.perc(0.001, 0.8, curve: -4).ar;
	//verb
	snd = snd + (GVerb.ar(snd, \roomsize.kr(1.3), \reverbtime.kr(3)) * -0.dbamp);

	snd = snd + ((snd * 20.dbamp).tanh * -10.dbamp);

	(1..3).do{ |i|
		snd = snd + LPF.ar(PitchShift.ar(snd, i.linlin(1, 3, 0.001, 0.003), i.linexp(1,3,1.4,2.6)) * -20.dbamp, 4000);
	};

	snd = snd * Env.perc(0.001, \dec.kr(0.8), curve: -4).kr(gate: Impulse.kr(0), doneAction: 2);
	snd = Pan2.ar(snd, \pan.kr(0));
	snd = snd* \gain.kr(-25).dbamp;
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\hh, {
	var snd;
	snd = WhiteNoise.ar([0.8, 0.7]);
	snd = BPF.ar(snd, (\freq.kr(1000) + ExpRand(4000, 8000)), 0.8);
	snd = CombL.ar(snd, 0.01, LFNoise1.kr(1).range(0.01, 0.1), 0.01);
	snd = snd + (GVerb.ar(snd, 4, 30));
	snd = snd * Env.perc(\atk.kr(0.001), \dec.kr(0.1), curve: -4).kr(gate: Impulse.kr(0), doneAction: 2);
	snd = snd * \gain.kr(-15).dbamp;
	snd = Pan2.ar(snd, \pan.kr(0));
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\reverb, {
	var sig;
	sig = In.ar(\in.ir(0), 2);
	sig = FreeVerb2.ar(sig[0], sig[1],0.1, 0.8, 0.8);
	Out.ar(\out.ir(0), sig);
}).play;
)

)

Synth(\kick);
Synth(\snare);
Synth(\hh);

t = TempoClock.new(112/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

t.schedAbs(t.nextBar, {t.beatsPerBar_()});
t.schedAbs(t.nextBar, {t.tempo_(160/60)});

///snare backbeat
(
p = Pbind(
	\instrument, \snare,
	\dur, Pseq([4, 4, 4, 1, 3], inf),
	\dec, Pseq([0.5, 0.5, 0.5, 1], inf),
	\freq, 60,
	\gain, -16,
);
y=p.play(t, quant:[1,1]);
)

y.stop;
x.stop;

(
~subdivideEach = { |patt, subdiv|

	subdiv = subdiv.asInteger;

	subdiv = subdiv.collect { |num| Array.fill(num, num) }.flatten;
	//patt = patt.collect { |num| Array.fill(subdiv.size, num) }.flatten;

	Pbind(
		\patt, Pseq(patt, inf),
		\subdiv, Pseq(subdiv , inf),
		\dur, Psubdivide(Pkey(\subdiv), Pkey(\patt)),
	)
};
)


Pdef(\rhythm, ~subdivideEach.value(#[1, 2, 3], #[4, 4, 4]));

Pdef(\rhythm, ~subdivideEach.value([1.5, 2, 1], [6, 4, 5]));

Pdef(\x, Pdef(\rhythm).trace <> Pbind(\instrument, \kick, \freq, 30)).play;

///////POLYPHONIC
(
var patt = #[1, 1, 0.5];
var subdiv = #[4, 3].asInteger;
var downbeat = 0.6;
var groupcount = 0;

subdiv = subdiv.collect { |num| Array.fill(num, num) }.flatten;
//patt = patt.collect { |num| Array.fill(subdiv.size, num) }.flatten;

p = Pbind(

    \subdiv, Pseq(subdiv , inf),

	\patt, Pseq(patt, inf),

	\dur, Psubdivide(Pkey(\subdiv), Pkey(\patt)),

	\deltatime, (Ptime() % t.beatsPerBar) / t.beatsPerBar,

	\groupdelta, Pfunc({
		|event|
		var groupdelta;
		groupcount = groupcount + 1;
		groupdelta = (groupcount - 1) / event[\subdiv];
		if(groupcount == event[\subdiv] ) { groupcount = 0 };
		groupdelta;
	}).trace,

    \instrument, Pfunc({ |event|
		var delta = event[\deltatime];
		x = case { delta >= 0.66 } { \kick }
		{ delta >= 0.33 } { \hh }
		{ delta >= 0 } { \kick2 };
		x;
    }),

	\freq, (abs(round(1 - Pkey(\groupdelta), downbeat) - Pkey(1 - \groupdelta))).linexp(0,1,30,60),
	\gain, (abs(round(Pkey(\groupdelta), downbeat) - (Pkey(\groupdelta)))).ampdb,
	\dec, (Pkey(\dur) + (round(Pkey(\groupdelta), downbeat) - Pkey(\groupdelta))),
	\pan, (round(Pkey(\deltatime), 1) - Pkey(\deltatime)),
);

x=p.play(t, quant:[1,0,0]);

)

////////PMONOARTIC
(
var patt = #[1, 1, 1];
var subdiv = #[3, 4, 4].asInteger;
var downbeat = 0.5;
var groupcount = 0;

subdiv = subdiv.collect { |num| Array.fill(num, num) }.flatten;
//patt = patt.collect { |num| Array.fill(subdiv.size, num) }.flatten;

p = PmonoArtic(
	\kick,

    \subdiv, Pseq(subdiv , inf),

	\patt, Pseq(patt, inf),

	\dur, Psubdivide(Pkey(\subdiv), Pkey(\patt)),

	\deltatime, (Ptime() % t.beatsPerBar) / t.beatsPerBar,

	\groupdelta, Pfunc({
		|event|
		var groupdelta;
		groupcount = groupcount + 1;
		groupdelta = (groupcount - 1) / event[\subdiv];
		if(groupcount == event[\subdiv] ) { groupcount = 0 };
		groupdelta;
	}).trace,

	\freq, (abs(round(1 - Pkey(\groupdelta), downbeat) - Pkey(1 - \groupdelta))).linexp(0,1,30,60),
	/*\gain, (abs(round(Pkey(\groupdelta), downbeat) - (Pkey(\groupdelta)))).trace.ampdb,
	\dec, max((Pkey(\dur) + (round(Pkey(\groupdelta), downbeat) - Pkey(\groupdelta))), 0.25),
	\pan, (round(Pkey(\deltatime), 1) - Pkey(\deltatime)),*/
);

x=p.play(t, quant:[1,0,0]);

)

// Function to modify the duration of events in a pattern
(
~modifyDuration = { |pattern, factor|
    pattern.collect { |event|
        event.use { |e|
			// e[\dur] = e[\dur] * factor; // Modify the duration
            e // Return the modified event
        }
    }
};