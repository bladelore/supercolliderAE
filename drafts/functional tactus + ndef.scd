(
//Server.default.options.device = "headphonesSc";
s.boot;
)
s.quit;

//NDEF
(
(
Ndef.clear;

Ndef(\restproxy, {
	Rest();
});

Ndef(\kick808, {arg out = 0, freq1 = 200, freq2 = 60, amp = 2, ringTime = 10, att = 0.001, rel = 1, dist = 0.0, pan = 0;
	var snd, env;
	snd = Ringz.ar(
		in: Impulse.ar(0), // single impulse
		freq: XLine.ar(freq1, freq2, 0.1),
		decaytime: ringTime);
	env = Env.perc(att, rel, amp).kr(doneAction: 2);
	snd = (1.0 - dist) * snd + (dist * (snd.distort));
	snd = snd * env * \gain.kr(0).dbamp;
	Out.ar(0, Pan2.ar(snd, pan));
});
)
)

Ndef(\kick808);

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
	//Pfunc({ |event| if(event[thiskey] == beat) { event } { event[\instrument] = \rest }});
	//.select({|event| event[\instrument] != \rest})
};
)

// Define a PSeq
//PATTERNS
(
//Pdef(\player).stop;

~p = Pseq(#[1.5, 2, 3, 4], 1);
~s = Pseq(#[7, 3, 4, 4, 2], 1);

Pdefn(\rhythm_kick, ~makeSubdivision.(~p, ~s));
Pdefn(\getDelta_kick, ~makeDeltas.(~pattern));
Pdefn(\groupselect_kick, ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1,4,2])).trace;

//Pdefn(\timingOffset_mod, Pbind(\timingOffset, Pkey(\bardelta) / Pkey(\dur) ));

Pdefn(\freqMod, Pbind(\freq1, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, 3).linlin(0, 1, 500, 30)));
Pdefn(\sweepMod, Pbind(\freq2, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.1, 3).linlin(0, 1, 40, 30)));
Pdefn(\gainMod, Pbind(\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0, -0.5).clip(0, 1).ampdb));
//Pdefn(\decayMod, Pbind(\rel, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 0.1, 10)));
Pdefn(\stretchMod, Pbind(\stretch, Pkey(\bardelta).linexp(0, 1, 0.125, 4)));
Pdefn(\tempoMod, Pbind(\tempo, Pkey(\bardelta).linexp(0, 1, 200/60, 60/60)));

//Ndef(\kick808).fadeTime=1;

Ndef(\kick808).fadeTime=0
Ndef(\kick808)[999]=Pbind(\dur, Pseq([0.5,0.5,0.5,0.5],inf), \deg, Pseq([1,1,1,\rest],inf)).trace;
Ndef(\kick808).play;
//Ndef(\kick808)[999] = \xset -> Pbind(\dur, Pseq([0.5,0.5,0.5,Rest(0.5)],inf));
/*Pdef(\kickPattern,
	//Pdefn(\sweepMod) <>
	//Pdefn(\decayMod) <>
	//Pdefn(\stretchMod) <>
	//Pdefn(\gainMod) <>
	//Pdefn(\freqMod) <>
	Pdefn(\groupselect_kick) <>
	Pdefn(\getDelta_kick) <>
	Pdefn(\rhythm_kick)
);*/

//Ndef(\kick808)[999] = \xset -> Pdef(\kickPattern).trace;

//playback
/*Pdef(\player,
	Ppar([Pdef(\kickPattern), Pdef(\hhPattern), Pdef(\snarePattern)])
).play(t, quant: [1,0]);
)*/
//end patterns
)

//change with pdefn redefs -- why does this only work with pmono
Pdefn(\rhythm_kick, ~makeSubdivision.(Pseq(#[1, 1, 2, 1], 1), Pseq(#[4, 2, 2], 1)));



//to do: rebuild using ndefs
//fx chain using ndefs
//macro sequencing

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

