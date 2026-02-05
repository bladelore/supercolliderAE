(
SynthDef(\rlpsaw, { arg out=0, amp=0.1, gate=1, pan=0, spread=0.8, freq=200,  doneAction=2;
    var sig, sig1, sig2, sig3;
    var env1, env2, env3, env4, env5, env6, env7, env8;
    env1 = EnvGen.kr(\lpadsr.kr(Env.adsr(0.1,0.1,0.8,0.1)), gate, doneAction:2);
    sig = LFSaw.ar(freq * [1.01,1,0.99]);
    sig = RLPF.ar(sig, \lpfr.kr(1.1).poll * freq * (env1 * \lpenvamt.kr(1) + 1), \rq.kr(0.5));
    sig = sig * EnvGen.ar(\adsr.kr(Env.adsr(0.01,0.1,0.8,0.1)),gate,doneAction:doneAction);
    sig = Splay.ar(sig, spread, amp, pan);
    Out.ar(out, sig);
}).add;

SynthDef(\modenvmono, { arg out=0, amp=0.1, gate=1;
    var sig;
    sig = EnvGen.kr(\env.kr(Env([1,1],[0.1])), \itrig.tr(1), doneAction:0);
    sig = sig * EnvGen.kr(Env([1,1,1],[0.1,0.8], releaseNode:1), gate, doneAction:2);
    Out.kr(out, sig);
}).add;
);

(
~modbus = ~modbus ?? { Bus.alloc(\control) };

Pdef(\part, Pdef(\zedpart,
    Ppar([
        Pbind(
            \instrument, \rlpsaw,
            \lpfr, ~modbus.asMap,
            \octave, 4,
            \note, Pseq([
                0,4,7,4
            ],inf),
            \dur, 1,
            \amp, 0.1,
        ),
        Pmono(\modenvmono,
            \env, Pseq([
                [ Env([0.1,4],[0.5]) ],
                [ Env([4,0.1],[0.5]) ],
                [ Env([0.1,1],[0.5]) ],
            ],inf),
            \itrig, 1,
            \dur, 1/2,
            \legato, 1,
            \out, ~modbus,
        ),
    ])
)).play;
);

//

(

Pdef(\bar2,
    Pbind(
        \instrument, \rlpsaw,
		\freq, ~pmodenv.(Pseq([100,2000,100,400],inf), Pseq([1,4,2]/4,inf), Pseq([1, 1, 1],inf), Pseq([-1, 0.5, 1],inf)),
		\pan, ~pmodenv.(Pseq([1,0,-1],inf), Pseq([4,4,2]/4,inf)),
        \legato, 0.5,
        \dur, 0.25,
        \amp, 0.1,
    )
).play;
);

(
SynthDef(\modenvmono, { arg out=0, amp=0.1, gate=1;
	var sig;
	sig = EnvGen.kr(\env.kr(Env([1,1],[0.1])), \itrig.tr(1), doneAction:0);
	sig = sig * EnvGen.kr(Env([1,1,1],[0.1,0.8], releaseNode:1), gate, doneAction:2);
	Out.kr(out, sig);s
}).add;

~pmodenv = { arg valpat, timepat=1, repeat=1, curvepat;
	// return a pattern that append a function to \finish which start a Pmono of modenvmono synth outputing an env curve according to patterns from function parameters
	repeat = repeat.clip(1,inf); // prevent infinite loop when puting negative curve in repeat
	Prout({ arg ev;
		var bus = Bus.control(s, 1);
		var timestr;
		var running = true;
		var cleanup = EventStreamCleanup.new;
		var patplayer;
		var finish_fun;
		var cleanup_fun = {
			patplayer.stop;
			running = false;
			//"pmodenv: CLEANUP".debug;
			{
				2.wait;
				if(bus.index.notNil) {
					bus.free;
				}
			}.fork;
		};

		timepat = timepat ??  { Plazy({ ev[\dur] }).loop };
		curvepat = curvepat ??  { 0 };

		cleanup.addFunction(ev, cleanup_fun);

		CmdPeriod.doOnce({
			if(bus.index.notNil) {
				bus.free
			}
		});

		finish_fun = {
			patplayer = Pmono(\modenvmono,
				\out, bus,
				\itrig, 1,
				[ \dur, \env ], Prout({ arg monoev;
					var valstr = valpat.asStream;
					var curvestr = curvepat.asStream;
					var previous = valstr.next;
					var time;
					timestr = timepat.asStream;

					block { arg break;
						valstr.do { arg val;
							var prev = previous;
							var curve;
							//val.debug("pmodenv val");
							time = timestr.next;
							curve = curvestr.next;
							if(time.isNil) {
								time = 2;
								monoev[\dur] = time;
								break.value;
							};
							if(curve.isNil) {
								time = 2;
								monoev[\dur] = time;
								break.value;
							};

							//monoev[\dur] = time;
							//Env([prev,val],[time]/thisThread.clock.tempo).asCompileString.debug("env");
							monoev = [time, [ Env([prev,val],[time]/thisThread.clock.tempo, curve) ]].yield;

							previous = val;
						};
					};
					running = false;
					monoev;
				}),
				\legato, 1,
			).play;
		};

		if(ev[\finish].isKindOf(Function)) {
			var oldfun = ev[\finish];
			ev[\finish] = { finish_fun.value; oldfun.value };
		} {
			ev[\finish] = finish_fun;
		};

		while{ running == true } {
			//"pmodenv running".debug;
			ev = bus.asMap.yield;
			//ev.debug("ev");
			if(ev.isNil) {
				"pmodenv: pattern ends".debug; // this never happen
				cleanup_fun.value;
			}
		};
		ev;
	}).repeat(repeat)
};
)

(
Pdef(\rlpsaw,
    Pbind(
        \instrument, \default,
		\freq, Pfunc({ Ndef(\lfoP, { LFSaw.kr(\lfofreq.kr(0.1))).linlin(-1, 1, 400, 1000) }).asMap }),
        \dur, 0.1,
        \amp, 0.1,
    )
).play;
);

(
Pdef(\zed,
    Pbind(
        \osc, Pseq([SinOsc, LFSaw],inf),
        \instrument, Prout({ arg ev;
            SynthDef(\zed, { arg out=0, gate=1, amp=0.1, pan=0, freq=200;
                var sig;
                sig = ev[\osc].ar(freq);
                sig = sig * EnvGen.kr(\adsr.kr(Env.adsr(0.1,0.1,0.8,0.1)), gate, doneAction:2);
                sig = Pan2.ar(sig, pan, amp);
                Out.ar(out, sig);
            }).add.yield;
        }).loop,
        \degree, Pseq([0],inf),
        \dur, 1,
        \amp, 0.1,
    )
).play;
);