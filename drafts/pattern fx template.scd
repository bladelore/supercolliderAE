(
SynthDef(\myfx, { arg out=0, gate=1;
    var sig;
    var in = InFeedback.ar(\inbus.kr(0), 2);
    sig = in;
    sig = sig * SinOsc.ar(\amfreq.kr(400));
    sig = SelectX.ar(\mix.kr(0.5), [in, sig]);
    sig = sig * EnvGen.kr(\adsr.kr(Env.adsr(0.1,0.1,0.8,0.1)), gate, doneAction:2);
    sig = sig * \gain.kr(1);
    Out.ar(out, sig);
}).add;

SynthDef(\longverb, { arg out=0, gate=1;
    var sig;
    var in = InFeedback.ar(\inbus.kr(0), 2);
    sig = in;
	sig = NHHall.ar(sig, rt60: \rt60.kr(1), stereo: 1, lowFreq: 200, lowRatio: 0.5, hiFreq: 4000, hiRatio: 0.5, earlyDiffusion: 0.5, lateDiffusion: 1, modRate: 0.2, modDepth: 0.2);
	sig = SelectX.ar(\mix.kr(0.5), [in, sig]);
	sig = sig * EnvGen.kr(\adsr.kr(Env.adsr(0.1,0.1,0.8,0.1)), gate, doneAction:2);
    sig = sig * \gain.kr(1);
    Out.ar(out, sig);
}).add;
);

(
    ~mygroup = Group();
    ~mybus = Bus.audio(s,2);
    ~myfx = Synth(\myfx, [ \inbus:~mybus ], target:~mygroup);

Pdef(\part,
    Ppar([
        Pbind(
            \instrument, \default,
            \freq, 200,
            \group, ~mygroup,
            \dur, 1,
            \out, ~mybus,
            \amp, 0.1,
        ),
        Pbind(
            \type, \set,
            \id, ~myfx.nodeID,
            \args, [\amfreq],
            \amfreq, ~pmodenv.(Pwhite(5000, 200,inf), Pseq([1], inf)),
			// \dur, 1/8,
        ),

    ])
).play;
);

(
Pdef(\part,
    Ppar([
        Pbind(
            \instrument, \default,
            \freq, 200,
            \dur, 1,
            \out, ~mybus,
            \amp, 0.1,
        ),
        Pmono(\myverb,
            \inbus, ~mybus,
            \addAction, \addToTail,
            \dur, 1,
            \mix, 0.5,
            \callback, { Pdefn(\fxid, ~id.debug("id")) },
        ),
        Pbind(
            \type, \set,
            \id, Pdefn(\fxid),
            \lag, 0.001,
            \args, [\rt60],
			// \dur, 1/8,
			\rt60, ~pmodenv.(Pexprand(100, 0.01,inf), Pkey(\dur)),
        ),

    ])
).play;
);


 ~rmbus = Bus.audio(s,2);

(
Pdef(\fxRm,
	Ppar([
        Pmono(\myfx,
            \inbus, ~rmbus,
            \addAction, \addToTail,
            \dur, 1,
            \mix, Pseq([1,0.3],inf),
            \callback, { Pdefn(\fxid, ~id.debug("id")) },
        ),
        Pbind(
            \type, \set,
            \id, Pdefn(\fxid),
            \lag, 0.001,
            \args, [\amfreq],
            \amfreq, Pseq([1000,300],inf),
            \dur, 1/8,
        )
	])
);
)

(
Pdef(\part,
    Ppar([
        Pbind(
            \instrument, \default,
            \freq, 200,
            \dur, 1,
            \out, ~rmbus,
            \amp, 0.1,
        ),
		Pdef(\fxRm)
    ])
).play;
);


(
Ndef(\pat)[0] = Pbind();
Ndef(\pat).play(~mybus, 2);
Ndef(\fx)[0] = \myfx;
Ndef(\fx)[1] = \pset -> Pdef(\fx, Pbind(\inbus, ~mybus, \amfreq, Pseq([1000,300],inf), \dur, 1/8));
Ndef(\fx).play;
Pbindef(\fx, \dur, 1/4);
Ndef(\fx)[1] = \pset -> Pdef(\fx, Pbind(\inbus, ~mybus, \amfreq, Pseq([100,200,300],inf), \dur, 1/8));
)