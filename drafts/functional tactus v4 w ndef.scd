(
//os.device = "headphonesSc";
o=s.options;
o.memSize;
o.hardwareBufferSize = 512;
o.blockSize = 512;
o.memSize = 2.pow(21);
s.boot;
)

s.quit;

(
SynthDef(\kick, {
	var snd, partialDecays;
	snd = SinOsc.ar(\freq.kr(30) * (1 + (\sweep.kr(2) * Env.perc(0.0, 0.13, curve: -4).ar)) * XLine.ar(1, 0.6 ,1) * [1, 1.3, 5.4, 4.8]);
	snd = snd * [0, -10, -5, -8].dbamp;
	partialDecays = (3..0).lincurve(0, 3, 0.01, 3, 5);
	snd = snd * Env.perc(0, partialDecays * \partials.kr(1)).ar;
	snd = snd.sum;
	snd = snd + (SinOsc.ar(XLine.ar(4000, 50, 0.01)) * Env.perc(0.0001, 0.01).ar * -5.dbamp);
	snd = snd + (BPF.ar(WhiteNoise.ar, 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	snd = snd + (BPF.ar(Hasher.ar(Sweep.ar), 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	snd = snd * (1 + (4 * Env.perc(0.01, 0.2).ar));
	//snd = snd + (GVerb.ar(snd, \roomsize.kr(8), \reverbtime.kr(1), spread: 16) * -30.dbamp);
	//snd = ((snd * -6.dbamp)).tanh;
	snd = snd * Env.perc(\atk.kr(0.01), \dec.kr(2), curve: -4).ar(gate: Impulse.ar(0), doneAction: 2);
	snd = snd * \gain.kr(-15).dbamp;
	snd = LeakDC.ar(in: snd, coef: 0.995);
	snd = Pan2.ar(snd, \pan.kr(0));
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\kick2, {
	var snd;
	snd = SinOsc.ar(\freq.kr(55) * (1 + (\sweep.kr(2) * Env.perc(0.0, 0.13, curve: -4).ar)) * XLine.ar(1, 0.6 ,1));
	snd = snd + (SinOsc.ar(XLine.ar(4000, 50, 0.01)) * Env.perc(0.0001, 0.01).ar * -5.dbamp);
	snd = snd + (BPF.ar(WhiteNoise.ar, 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	snd = snd + (BPF.ar(Hasher.ar(Sweep.ar), 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	snd = snd * (1 + (4 * Env.perc(0.01, 0.2).ar));
	//snd = snd + (GVerb.ar(snd, \roomsize.kr(8), \reverbtime.kr(1), spread: 16) * -30.dbamp);
	//snd = ((snd * 10.dbamp)).tanh;
	snd = snd * Env.perc(\atk.kr(0.01), \dec.kr(2), curve: -3).ar(Done.freeSelf);
	snd = snd * \gain.kr(-15).dbamp;
	snd = Pan2.ar(snd, \pan.kr(0));
	snd = LeakDC.ar(in: snd, coef: 0.995);
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\snare, {
	var snd, noiseArr;
	snd = SinOsc.ar(\freq.kr(200) * (1 + (2 * Env.perc(0.001, 0.03).ar)) * ([1, 1.3, 2.5, 5.8]));
	snd = snd * Env.perc(0, [2, 0.7, 0.7, 0.8]).ar;
	//snd = snd * [0, -20, -12, -24].dbamp;
	snd = snd.sum;
	snd = snd + (SinOsc.ar(XLine.ar(20000, 100, 0.03)) * Env.perc(0.01, 0.03).ar * -3.dbamp);
	snd = snd + (BPF.ar(Hasher.ar(Sweep.ar), 2120, 1.2) * Env.perc(0.05, 0.1).ar * -3.dbamp);
	snd = snd + (BPF.ar(Hasher.ar(Sweep.ar), 9120, 2) * Env.perc(0.003, 0.15).ar * -8.dbamp);
	snd = snd * Env.perc(0.001, 0.8, curve: -4).ar;
	//verb
//	snd = snd + (GVerb.ar(snd, \roomsize.kr(1.3), \reverbtime.kr(3)) * -20.dbamp);
/*	(1..3).do{ |i|
		snd = snd + LPF.ar(PitchShift.ar(snd, i.linlin(1, 3, 0.001, 0.003), i.linexp(1,3,1.4,2.6)) * -20.dbamp, 4000);
	};*/

	//snd = snd + ((snd * 20.dbamp).tanh * -2.dbamp);

	snd = snd * Env.perc(0.001, \dec.kr(0.8), curve: -8).kr(gate: Impulse.ar(0), doneAction: 2);
	snd = LeakDC.ar(in: snd, coef: 0.995);
	snd = Pan2.ar(snd, \pan.kr(0));
	snd = snd* \gain.kr(-25).dbamp;

	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\hh, {
	var snd;
	snd = WhiteNoise.ar([0.8, 0.7]);
	snd = BPF.ar(snd, (\freq.kr(1000) + ExpRand(4000, 8000)), 0.8);
	snd = CombL.ar(snd, 0.01, LFNoise1.kr(1).range(0.01, 0.1), 0.01);
	//snd = snd + (GVerb.ar(snd, 4, 30));
	snd = snd * Env.perc(\atk.kr(0.001), \dec.kr(0.1), curve: -4).ar(Done.freeSelf);
	snd = LeakDC.ar(in: snd, coef: 0.995);
	snd = Pan2.ar(snd, \pan.kr(0));
	Out.ar(\out.kr(0), snd);
}).add;
)

t.schedAbs(t.nextBar, {t.beatsPerBar_(7)});
t.schedAbs(t.nextBar, {t.tempo_(100/60)});

///COMPOSTION FUNCTIONS
(
//CLOCK
t = TempoClock.new(160/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

//to do
//metric modulation
//accel/decel by phase?
//swing/swing by group
//interpolate between patterns by over x bars
//choke groups
//add risers to hits
//sequence sections

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
};
)


// Define a PSeq
//PATTERNS
(
Ndef.clear;
~p = Pseq(#[3, 1.5, 1, 1.5, 4], 1);
~s = Pseq(#[3, 4, 4, 2], 1);

Pdefn(\rhythm_kick, ~makeSubdivision.(~p, ~s));
Pdefn(\rhythm_hh, ~makeSubdivision.(~p, Pseq(#[3, 6, 12], 1)));

Pdefn(\getDelta_kick, ~makeDeltas.(~pattern));
Pdefn(\groupselect_kick, ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1,4,2]));

//Pdefn(\timingOffset_mod, Pbind(\timingOffset, Pkey(\bardelta) / Pkey(\dur) ));
Pdefn(\atkMod, Pbind(\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.75, -1).linexp(0, 1, 0.01, 0.5)));
Pdefn(\freqMod, Pbind(\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 30, 80)));
Pdefn(\gainMod, Pbind(\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0, -0.5).clip(0, 1).ampdb));
Pdefn(\decayMod, Pbind(\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2)));
Pdefn(\sweepMod, Pbind(\sweep, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.1, 3).linexp(0, 1, 8, 0.1)));
//
Pdefn(\freqMod_hh, Pbind(\freq, Pkey(\bardelta).linexp(0, 1, 30, 20000)));
Pdefn(\legatoMod_hh, Pbind(\legato, Pkey(\groupdelta).linlin(0, 1, 0.1, 0.8)));
Pdefn(\groupselect_hh, ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[2,3]));

Pdefn(\stretchMod, Pbind(\stretch, Pkey(\bardelta).linexp(0, 1, 0.125, 4)));
Pdefn(\tempoMod, Pbind(\tempo, Pkey(\bardelta).linexp(0, 1, 200/60, 60/60)));
)

(
Ndef.clear;
Ndef(\lfo, { LFSaw.kr(\lfofreq.kr(0.1)) });
Ndef(\lfo2, { LFSaw.kr(\lfofreq.kr(0.5)) });
)

(
Ndef(\mykick).fadeTime = 0;
Ndef(\mykick).clock = t;
//kick
Ndef(\mykick)[0] = Pdef(\kickPattern,
	//PmonoArtic(\kick2) <>
	Pdefn(\sweepMod) <>
	Pdefn(\decayMod) <>
	//Pdefn(\stretchMod) <>
	//Pdefn(\gainMod) <>
	Pdefn(\freqMod) <>
	Pdefn(\groupselect_kick) <>
	Pdefn(\atkMod) <>
	Pdefn(\getDelta_kick) <>
	Pdefn(\rhythm_kick) <>
	Pdefn(\kickParams, Pbind(\instrument, \kick2, \gain, -6));
);
/*Ndef(\mykick).xset(\wet1, 0.5);
Ndef(\mykick)[1] = \filter -> { |in| JPverb.ar(
        in,
        \t60.kr(1,           0.05),
        \damp.kr(0,          0.05),
        \size.kr(1,          0.05),
        \earlydiff.kr(0.707, 0.05),
        \mdepth.kr(5,        0.05),
        \mfreq.kr(2,         0.05),
        \lowx.kr(1,          0.05),
        \midx.kr(1,          0.05),
        \highx.kr(1,         0.05),
        \lowband.kr(500,     0.05),
        \highband.kr(2000,   0.05)
    )};
Ndef(\mykick).map(\earlydiff, Ndef(\lfo), );*/
Ndef(\mykick).play;
)

(
//hat
Ndef(\myhat).clock = t;
Ndef(\myhat).fadeTime = 0;
Ndef(\myhat)[0] = Pdef(\hhPattern,
	PmonoArtic(\hh) <>
	//Pdefn(\stretchMod) <>
	Pdefn(\decayMod) <>
	Pdefn(\freqMod_hh) <>
	//Pdefn(\legatoMod_hh) <>
	Pdefn(\groupselect_hh) <>
	Pdefn(\getDeltas_hh, ~makeDeltas.(~deltapattern)) <>
	Pdefn(\rhythm_hh) <>
	Pdefn(\hhParams, Pbind(\instrument, \hh, \gain, -6));
);
/*
Ndef(\myhat)[1] = \filter -> { |in| JPverb.ar(
	in,
	\t60.kr(1,           0.05),
	\damp.kr(0,          0.05),
	\size.kr(1,          0.05),
	\earlydiff.kr(0.707, 0.05),
	\mdepth.kr(5,        0.05),
	\mfreq.kr(2,         0.05),
	\lowx.kr(1,          0.05),
	\midx.kr(1,          0.05),
	\highx.kr(1,         0.05),
	\lowband.kr(500,     0.05),
	\highband.kr(2000,   0.05)
)};
//
Ndef(\myhat)[2] = \filter -> { |in| PitchShift.ar(in, 0.25, \pitch.kr(2), \pitchdispersion.kr(0.01), \timedispersion.kr(0.001))};
Ndef(\myhat).xset(\wet1, 0.3, \wet2, 0.6);
Ndef(\myhat).map(\timedispersion, Ndef(\lfo), \pitch, Ndef(\lfo2), \wet1, Ndef(\lfo), \size, Ndef(\lfo2), \pan, Ndef(\lfo));*/
//Ndef(\myhat)[1] = \xset -> Pdef(\modpatt, Pbind(\pitchdispersion, Pseq([0.5, 0.3, 0.2, 1],inf)));
Ndef(\myhat).play;
)

(
//snare
Ndef(\mysnare).fadeTime = 0;
Ndef(\mysnare).clock = t;
Ndef(\mysnare)[0] = Pdef(\snarePattern,
	//Pdefn(\freqMod) <>
	Pdefn(\decayMod) <>
	Pdefn(\groupselect_snare, ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[2, 4]))<>
	Pdefn(\getDeltas_snare, ~makeDeltas.(~deltapattern)) <>
	Pdefn(\rhythm_snare, ~makeSubdivision.(~p, Pseq(#[1, 2, 4], 1))) <>
	Pdefn(\snareParams, Pbind(\instrument, \snare, \gain, 0, \freq, 30))
);
/*Ndef(\mysnare)[1] = \filter -> { |in| Greyhole.ar(
	in,
	\dtime.kr(0.1),
	\damp.kr(0.1),
	\size.kr(1),
	\diff.kr(2),
	\fb.kr(0.8),
	\modDepth.kr(0.01),
	\modFreq.kr(2)
)};

Ndef(\mysnare).xset(\wet1, 0.4);
Ndef(\mysnare).map(\size, Ndef(\lfo2), \dtime, Ndef(\lfo1));*/
Ndef(\mysnare).play;
)

Ndef(\mykick).play;
Ndef(\mysnare).play;
Ndef(\myhat).play;

Ndef(\mykick).stop;
Ndef(\myhat).stop;
Ndef(\mysnare).stop;

//change with pdefn redefs -- why does this only work with pmono
Pdefn(\rhythm_kick, ~makeSubdivision.(Pseq(#[4, 1, 2, 1], 1), Pseq(#[8, 4, 5], 1)));

(
Ndef(\lfo3, { LFSaw.kr(\lfofreq.kr(0.01)).bipolar * 0.2});
Ndef(\padFreqs, {([404, 404/2, 404/5, 404/7])+LFNoise2.kr(0.1!4, 15)});
Ndef(\padAmps, {[0.4, 0.3, 0.2, 0.5]+LFNoise2.kr(0.1!4, 0.1)});
Ndef(\padCut, {(10000 * Ndef.kr(\lfo3)).clip(20,20000)});
Ndef(\padQ, 0.8);

Ndef(\pad, {
	LeakDC.ar(
	Splay.ar(
		BLowPass4.ar(
			Blip.ar(
				Ndef.ar(\padFreqs),
				Ndef.ar(\padAmps)
			),
			Ndef.ar(\padCut),//cutoff freq for filter
			Ndef.ar(\padQ)//q for filter
		)
	)) * 0.5
})
)


Ndef(\pad)[1] = \filter -> { |in| PitchShift.ar(in, 0.25, \pitch.kr(1.5), \pitchdispersion.kr(0.01), \timedispersion.kr(0.001))};
Ndef(\pad).map(\pitchdispersion, Ndef(\lfo3));

(
Ndef(\pad)[3] = \filter -> { |in| JPverb.ar(
        in,
        \t60.kr(1,           0.05),
        \damp.kr(0,          0.05),
        \size.kr(10,          0.05),
        \earlydiff.kr(0.707, 0.05),
        \mdepth.kr(5,        0.05),
        \mfreq.kr(2,         0.05),
        \lowx.kr(1,          0.05),
        \midx.kr(1,          0.05),
        \highx.kr(1,         0.05),
        \lowband.kr(500,     0.05),
        \highband.kr(2000,   0.05)
)};
)

Ndef(\pad).play(fadeTime:10);

Ndef(\pad).stop;



