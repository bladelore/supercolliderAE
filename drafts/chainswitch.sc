
PbindGenerator(\membraneLo)
(
Pdef(\kick, 
	Pbind(
		\instrument, \fmPerc3,
		\freq, Pseq([60.0, 90], inf),
		\atk, 0.01,
		\dec, Pkey(\dur) * 1.5,
		\fb, 0.4,
		\index1, 1,
        \index2, 2,
		\ratio1, 0.5,
        \ratio2, 2,
		\drive, 0,
        \drivemix, 0,
		\sweep, 8.0,
		\spread, 20.0,
		\noise, 0.25,
		\feedback, Pkey(\cycledelta),
		\fbmod, 1,
		\pulseWidth, 1 - Pkey(\groupdelta).linlin(0,1,0.1,0.99),
		\lofreq, 500.0,
		\lodb, 10.0,
		\midfreq, 1200.0,
		\middb, 0.0,
		\hifreq, 7000.0,
		\hidb, 30.0,
		\gain, -17.0,
		\pan, 0.0,
		\amp, Pkey(\groupdelta).linlin(0, 1, 1, 0.3)
	)
);

Pdef(\snare, 
	Pbind(
		\instrument, \rim1,
		// \gate, 1.0,
		\freq, 50.0,
		\atk, 0.03,
		\dec, Pkey(\dur),
		\fb, 10.0,
		\index, 4.0,
		\ratio, 16.0,
		\sweep, 8.0,
		\spread, 20.0,
		\noise, 1.0,
		\roomsize, 3.0,
		\reverbtime, 5.0,
		\gain, -20.0,
		\pan, 0.0,
		\amp, Pkey(\groupdelta).linlin(0, 1, 1, 0.3),
		// \amp, 0
	)
);

Pdef(\rim, 
	Pbind(
		\instrument, \rim1,
		// \gate, 1.0,
		\freq, 60,
		\atk, 0.03,
		\dec, 0.1,
		\fb, 0.0,
		\index, 4.0,
		\ratio, 16,
		\sweep, 128.0,
		\spread, Pkey(\groupdelta).linlin(0,1,1,20),
		\noise, 1.0,
		\roomsize, 3.0,
		\reverbtime, 5.0,
		\gain, -20.0,
		\pan, 0.0,
		\amp, Pkey(\groupdelta).linlin(0, 1, 1, 0.3),
		// \amp, 0
	)
);

Pdef(\hh, 
	Pbind(
		\instrument, \hh2,
		\freq, 1200.0,
		// \fb, 1 - Pkey(\groupdelta),
		\spread, 20.0,
		\index, 0.75,
		\pulseWidth, Pkey(\groupdelta),
		\atk, 0.001,
		\dec, Pkey(\dur),
		\gain, -5.0,
		\pan, 0.0,
		// \amp, 0
		\amp, Pkey(\groupdelta),
	)
);

Pdef(\tick, 
	Pbind(
		\instrument, \tick,
		\freq, 1200.0,
		\fb, 0.5,
		\spread, 0.0,
		\atk, 0.001,
		\dec, Pkey(\dur) * 4,
		\pan, 0.0,
		\amp, 1.0,
	)
);
)

(
t = TempoClock.new(140/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

Pdef(\p1,
	~makeSubdivision.(
		PlaceAll([1.5, Prand([1, 2], inf), 1], inf),
		PlaceAll([6, 4, 4], inf)
	)	
);

// Pdef(\p1, 
// 	~makeSubdivision.(
// 		PlaceAll([1.5, 1.5, 1], inf),
// 		PlaceAll([3, 3, 4], inf)
// 	)
// );

// Pdef(\p1,
// 	~makeSubdivision.(
// 		PlaceAll([1, 1, Rest(1)], inf),
// 		PlaceAll([2, 2, 2, 4], inf)
// 	)
// );

Pdef(\p2,
	// ~pChainSwitch.([Pdef(\kick), Pdef(\hh), Pdef(\snare), Pdef(\rim)], PlaceAll([1, 2, 2, 1, 2, 2, 2, [3, 4]], inf))
	// ~pChainSwitch.([Pdef(\kick), Pdef(\hh), Pdef(\snare)], Pseq([1, 2, 2, 1, 2, 2, 2, 3], inf))
	// ~pChainSwitch.([Pdef(\kick), Pdef(\hh), Pdef(\snare)], Pseq([1, 1, 2, 1, 2, 2, 2, 3], inf)))
	~pChainSwitch.([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\snare)], PlaceAll([1, 1, 2, 1, 2, 1, 2, 3], inf)))
	<> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2], skew: [1], curve: \exp)
	<> ~filterBeat.(key: Pkey(\groupcount), beat:[2, 3], reject: 1)
	<> Pdef(\p1)
).play(t);
)

PbindGenerator(\rim1)