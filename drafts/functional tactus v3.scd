s.boot;
s.quit;

(
SynthDef(\kick, {
	var snd, partialDecays;
	//sine osc * freq sweep
	//freq sweep converges to base freq * XLine for extra freq sweep down
	//multichannel expand array
	snd = SinOsc.ar(\freq.kr(30) * (1 + (\sweep.kr(2) * Env.perc(0.0, 0.13, curve: -4).ar)) * XLine.ar(1, 0.6 ,1) * [1, 1.3, 5.4, 4.8]);
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
	snd = snd * Env.perc(\atk.kr(0.001), \dec.kr(0.1), curve: -4).ar(gate: Impulse.kr(0), doneAction: 2);
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

//t.schedAbs(t.nextBar, {t.beatsPerBar_(4)});
t.schedAbs(t.nextBar, {t.tempo_(170/60)});

///COMPOSTION FUNCTIONS
(
//CLOCK
t = TempoClock.new(160/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

//to do
//metric modulation
//accel/decel by phase?
//swing/swing by group
//interpolate between patterns by over x bars

~makeSubdivision = { |patt, subdiv|
	var cycleLength = subdiv.asStream.all.size;

	patt = Pn(patt, inf);
	subdiv = subdiv.asInteger;
	subdiv = Pdup(subdiv, subdiv);

	Pbind(
		\subdiv, Pn(subdiv , inf),
		\dur, Psubdivide(Pkey(\subdiv), patt),
		\cycle, Pn(cycleLength, inf),
	)
};

~makeDeltas = { |patt|
	var eventcount = 0;
	var groupcount = 0;
	//var groupIdxCount
	Pbind(
		//delta time in relation to tempo clock
		\bardelta, (Ptime() % t.beatsPerBar) / t.beatsPerBar,
		//janky ass subroutine
		//group delta time
		\groupdelta, Pfunc({|event|
			var groupdelta;
			//event index in cycle
			event[\eventcount] = eventcount + 1;
			eventcount = eventcount + 1;
			//as delta within group
			groupdelta = (eventcount - 1) / event[\subdiv];

			//index of group
			if(groupcount == event[\cycle]) {
				groupcount = 0;
			};
			//set current group count at key
			event[\groupcount] = groupcount + 1;
			//reset event and increment group count
			if(eventcount == event[\subdiv] ) {
				eventcount = 0;
				groupcount = groupcount + 1;
			};

			groupdelta;
		}),
	)
};

~nearestDelta = { |patt, key, downbeat, centre|
	//wtf does this do tbh
	abs(round(centre - key, downbeat) - (centre - downbeat));
};

~filterBeat = { |patt, key, beat|
	var thiskey = key.key;
	Pfunc({ |event| if(beat.includes(event[thiskey])) { event } { event[\instrument] = \rest }});
	//Pfunc({ |event| if(event[thiskey] == beat) { event } { event[\instrument] = \rest }});
	//.select({|event| event[\instrument] != \rest})
};
)

// Define a PSeq
//PATTERNS
(
Pdef(\player).stop;

~p = Pseq(#[1.5, 2, 3, 4], 1);
~s = Pseq(#[3, 3, 4, 4, 2], 2);

//~p = Pseq(#[1, 1.5, 0.5], 1);
//~s = Pseq(#[2, 4], 2);

Pdefn(\rhythm_kick, ~makeSubdivision.(~p, ~s));
Pdefn(\rhythm_hh, ~makeSubdivision.(~p, Pseq(#[3, 6, 12], 1)));

Pdefn(\getDelta_kick, ~makeDeltas.(~pattern));
Pdefn(\groupselect_kick, ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1,4,2]));

//Pdefn(\timingOffset_mod, Pbind(\timingOffset, Pkey(\bardelta) / Pkey(\dur) ));

Pdefn(\freqMod, Pbind(\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 30, 50)));
Pdefn(\gainMod, Pbind(\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0, -0.5).clip(0, 1).ampdb));
Pdefn(\decayMod, Pbind(\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.1, 1).linexp(0, 1, 1, 0.2)));
Pdefn(\sweepMod, Pbind(\sweep, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.1, 1).linexp(0, 1, 8, 0.1)));

Pdefn(\freqMod_hh, Pbind(\freq, Pkey(\bardelta).linexp(0, 1, 30, 20000)));
Pdefn(\legatoMod_hh, Pbind(\legato, Pkey(\groupdelta).linlin(0, 1, 0.1, 0.8)));
Pdefn(\groupselect_hh, ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[2,3]));

Pdefn(\stretchMod, Pbind(\stretch, Pkey(\bardelta).linexp(0, 1, 0.125, 4)));
Pdefn(\tempoMod, Pbind(\tempo, Pkey(\bardelta).linexp(0, 1, 200/60, 60/60)));

(
Pdef(\kickPattern,
	Pdefn(\sweepMod) <>
	//Pdefn(\decayMod) <>
	Pdefn(\stretchMod) <>
	Pdefn(\gainMod) <>
	Pdefn(\freqMod) <>
	//Pdefn(\groupselect_kick) <>
	Pdefn(\getDelta_kick) <>
	Pdefn(\rhythm_kick) <>
	Pdefn(\kickParams, Pbind(\instrument, \kick, \gain, 0));
);
//).play(t, quant:[1,0]);

Pdef(\hhPattern,
	PmonoArtic(\hh) <>
	Pdefn(\stretchMod) <>
	//Pdefn(\tempoMod)<>
	Pdefn(\decayMod) <>
	Pdefn(\freqMod_hh) <>
	Pdefn(\legatoMod_hh) <>
	Pdefn(\groupselect_hh) <>
	Pdefn(\getDeltas_hh, ~makeDeltas.(~deltapattern)) <>
	Pdefn(\rhythm_hh) <>
	Pdefn(\hhParams, Pbind(\gain, -20));
);
//).play(t, quant:[1,0]);

Pdef(\snarePattern,
	Pdefn(\freqMod) <>
	Pdefn(\decayMod) <>
	Pdefn(\getDeltas_snare, ~makeDeltas.(~deltapattern)) <>
	Pdefn(\rhythm_snare, ~makeSubdivision.(~p, Pseq(#[1, 2, 4], 1))) <>
	Pdefn(\groupselect_snare, ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[2, 4]));
	Pbind(
	\instrument, \snare,
	\dur, Pseq([4, 4, 4, 1, 3], inf, 1),
	\dec, Pseq([0.5, 0.5, 0.5, 1], inf, 0.5) * 0.5,
	\freq, 30,
	\gain, -13,
	)
);

//playback
Pdef(\player,
	Ppar([Pdef(\kickPattern), Pdef(\hhPattern), Pdef(\snarePattern)])
).play(t, quant: [1,0]);
)
//end patterns
)

(
Ndef(\padFreqs, {([404, 404/2, 404/5] * 0.5)+LFNoise2.ar(0.1!4, 30)});
Ndef(\padAmps, {[0.4, 0.3, 0.2, 0.5]+LFNoise2.ar(0.1!4, 0.1)});
Ndef(\padCut, 5000);
Ndef(\padQ, 0.8);
Ndef(\pad, {
	Splay.ar(
		BLowPass4.ar(
			Blip.ar(
				Ndef.ar(\padFreqs),
				Ndef.ar(\padAmps)
			),
			Ndef.ar(\padCut),//cutoff freq for filter
			Ndef.ar(\padQ)//q for filter
		) * 0.5
	)
}).play(fadeTime:10);
)

Ndef(\pad).stop;
