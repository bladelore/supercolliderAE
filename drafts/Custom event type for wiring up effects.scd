(
// add custom event
Event.addEventType(\nbind, {arg server;

	var id = (currentEnvironment[\ndef] ? currentEnvironment[\instrument]).asSymbol;
	var type = \note;

	if (Ndef(id).isPlaying.not) {
		Ndef(id).play;
	};

	~type = type;
	~group = Ndef(id).group;
	~out = Ndef(id).bus;

    currentEnvironment.play;
});

// synth
SynthDef(\synth1, {arg freq = 440, out = 0;

	var env = Env.perc.kr(doneAction:2);
	var sig = SinOscFB.ar(freq * [1, 1.01], 0.7);
	sig = sig * env;
	OffsetOut.ar(out, Splay.ar(sig));

}).add;

// set up first set of fx
Ndef(\fx1).play;
Ndef(\fx1)[0] = \filter -> {arg in;
	in.abs;
};

// set up additional fx
Ndef(\fx2).play;
Ndef(\fx2)[0] = \filter -> {arg in;
	in + AllpassC.ar(in, 0.02, 0.02, [3, 5]);
};

)

(
// play a pattern
Pbind(\instrument, \synth1,
	// specify custom event type
	\type, \nbind,
	// switch between fx
	\ndef, Pseq([\fx1, \fx2], inf),
	\degree, Pwhite(0, 5, inf)
).play;
)