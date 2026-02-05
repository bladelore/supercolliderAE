(
~mod1_in = Bus.audio(s, 2);
~mod1_out = Bus.control(s, 1);
)

(
Pdef(\follower1,
	Pmono(\envfollow,
		// \dur, 1,
		\amp, 0.1,
		\mul, 10000,
		\add, 300,
		\inbus, ~mod1_in,
		\out, ~mod1_out,
	)
);

Pdef(\zedpart,
	Ppar([
		Pbind(
			\instrument, \default,
			\freq, ~mod1_out.asMap,
			\dur, 1,
			\amp, 0.1,
			\out, [~mainout, ~mod1_in],
		),
		Pdef(\follower1)
	])
).play;
)

(
SynthDef(\envfollow, { |gate=1|
    var sig;
    var in = InFeedback.ar(\inbus.kr(0), 2);
    sig = in;
	// sig = EnvFollow.ar(sig.mean);
	sig = Amplitude.ar(sig, \atk.kr(0.01), \rel.kr(0.1));
	// sig = sig * EnvGen.kr(\adsr.kr(Env.adsr(0.1,0.1,0.8,0.1)), gate, doneAction:2);
    sig = sig * \gain.kr(1);
    sig = sig * \mul.kr(1) + \add.kr(0);
    Out.kr(\out.kr(0), sig);
}).add;
);

