(
// example synthdef
SynthDef(\lead2, {	arg out=0, freq = 100, pan=0, amp=0.1, mdetune=1.004, gate=1, rq=0.1, fratio = 1, fbase=400, wet=1, fbfreq=100, fbamp=0.8, fbpamp=1, rt=0.4;
	var fb, ou, filtenv;
	ou = LFSaw.ar(freq * [1, mdetune]).sum;
	filtenv = EnvGen.ar(Env.adsr(0.01,0.25,0.07,0.3), gate, freq * fratio, fbase, doneAction:0);
	ou = RLPF.ar(ou, filtenv, rq);
	fb = LocalIn.ar(1) + ou;
	fb = HPF.ar(fb, fbfreq);
	LocalOut.ar(fb * fbamp);
	fb = Limiter.ar(fb, amp);
	fb = SelectX.ar(wet, [ou, fb*fbpamp]);
	fb = fb * EnvGen.ar(Env.adsr(0.001,0.4,0.9,rt), gate, doneAction:2);
	fb = Pan2.ar(fb, pan, amp);
	Out.ar(out, fb);
}).store;
)

(
// event morphing
~morph = { arg ev_start, ev_end, time, repeat=1;
	var ev_res;

	if(ev_start.isArray) { ev_start = Event.newFrom(ev_start) };
	if(ev_end.isArray) { ev_end = Event.newFrom(ev_end) };

	ev_res = ();
	ev_start.keys.do { arg key;
		ev_res[key] = Pseg(Pseq([ev_start[key], ev_end[key]],repeat), time);
	};
	Pbind(*ev_res.asKeyValuePairs);
};

// pattern morphing
~morphpat = { arg pat1, pat2, mpat;
	var spat1, spat2, smpat;
	var ev1, ev2, morph;
	var res_ev;
	var rout;
	var exclu = Set[\dur, \instrument];
	spat1 = pat1.asStream;
	spat2 = pat2.asStream;
	smpat = mpat.asStream;
	rout = Routine {
		block { arg break;
			loop {
				ev1 = spat1.next(Event.default);
				ev2 = spat2.next(Event.default);
				morph = smpat.next;
				if(ev1.isNil or: { ev2.isNil or: { morph.isNil }}) {
					break.value;
				} {
					res_ev = ();
					ev1.keys.difference(exclu).do { arg key;
						res_ev[key] = (ev1[key] * morph) + (ev2[key] * (1 - morph));
					};
					res_ev.debug("res_ev");
					res_ev.yield;
				}
			}
		}
	};
	rout
};

)

// we want to morph from this pattern
(
Pdef(\plop, Pbind(
	\instrument, \lead2,
	\degree, Pseq([0,2,4],inf),
	\fratio, 9,
	\sustain, 0.1,
	\wet, 0.8,
	\fbfreq, 4100,
	\fbpamp, 4,
	\rq, 0.2,
	\dur, 0.25,
	\amp, 0.1
)).play;
);

// to this pattern
(
Pdef(\plop, Pbind(
	\instrument, \lead2,
	\degree, Pseq([0,2,4],inf),
	\fratio, 2.4,
	\sustain, 0.21,
	\wet, 0.1,
	\fbfreq, 4180,
	\fbpamp, 1,
	\rq, 2.01,
	\dur, 0.25,
	\amp, 0.2
)).play;
);


(
// event morphing
Pdef(\plop, Pbind(
	\instrument, \lead2,
	\degree, Pseq([0,2,4,Prand([0,2,4,7,9,-5,-3])],inf),
	\mtranspose, Pstep(Pseq([0,3,1,4, 0,2,1,3],inf),2),
	//\mtranspose, Pstep(Pseq([0,2,1,3],inf),4),
	\dur, 0.25,
	\sustain, Pkey(\sustain) * 4,
	\amp, 0.1
) <> ~morph.([ // work also with event notation
	\fratio, 2.4,
	\sustain, 0.21,
	\wet, 0.1,
	\fbfreq, 180,
	\fbpamp, 1,
	\rq, 2.01,
], [
	\fratio, 9,
	\sustain, 0.1,
	\wet, 0.8,
	\fbfreq, 4800,
	\fbpamp, 4,
	\rq, 0.2,
], 10,inf)).play; // third argument: duration of the morphing, 4th argument: repeat
)


(
// pattern morphing
~pat1 = Pbind(
	\fratio, Pseq([10,2.4],inf),
	\sustain, 0.21,
	\wet, 0.1,
	\fbfreq, Pseq([4000,180],inf),
	\fbpamp, 1,
	\rq, 2.01,
);

~pat2 = Pbind(
	\fratio, 9,
	\sustain, 0.1,
	\wet, 0.8,
	\fbfreq, Pseq([200,4800],inf),
	\fbpamp, 4,
	\rq, 0.2,
);
p = ~morphpat.(~pat1, ~pat2, Pseg(Pseq([0, 1, 0.5],inf),10)); // third argument: 0 = first pattern, 1 = second pattern
(Pbind(
	\instrument,\lead2,
	\degree, Pseq([0,2,4,Prand([0,2,4,7,9,-5,-3])],inf),
	\mtranspose, Pstep(Pseq([0,3,1,4, 0,2,1,3],inf),2),
	\sustain, Pkey(\sustain) * 4,
	\dur, 0.25,
) <> p).trace.play;
)