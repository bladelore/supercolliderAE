(
~pfadeC = { |a, b, dur, curve=\linear|

	var env = Pseq([0 + 1e-4, 1], 1);

	Ppar([
		Pbindf(a, \amp, ~pmodenv.(Pseq([Pseq([0 + 1e-4, 1], 1), Pseq([1], inf)]), dur, 1, curve)),
		Pbindf(b, \amp, ~pmodenv.(1 - env, dur, 1, curve)),
	]);
}
)

(
~pfadeD = { |a, b, dur, curve=\linear|
	var env = Pseg(Pseq([0 + 1e-4, 1], 1), dur, curve);

	Ppar([
		Pbindf(a, \amp, Pseq([env, Pseq([1], inf)]) ),
		Pbindf(b, \amp, 1 - env),
	]);
}
)


(
~pfadeX = { |a, b, dur|
	Ppar([
		PfadeIn(a, dur),
		PfadeOut(b, dur),
	]);
}
)

~pchanceFade.(Pdef(\player3), Pdef(\player4), 32).play(t);

(
~pchanceFade = { |a, b, dur, curve='linear'|
	var env = Env.new([0 + 1e-6, 1], dur, curve).asStream;
	// var env = Pn(Pseg(Pseq([0 + 1e-4, 1], 1), dur, curve, inf)).asStream;
	var chance = Pwhite(0.0, 1.0, inf).asStream;

	Ppar([
		Pbindf(a, \instrument,
			Pfunc({|ev|
				var val;
				if(chance.next < env.next.postln)
				{ val = \rest }
				{ val = ev[\instrument]};
				val;
			})
		)
		,

		Pbindf(b, \instrument,
			Pfunc({|ev|
				var val;
				if(chance.next > env.next)
				{ val = \rest }
				{ val = ev[\instrument]};
				val;
			})
		)
	]);
}
)

(
~pFade = { |a, b, dur, curve='linear'|
	var env = Env.new([0 + 1e-6, 1], dur, curve).asStream;
	// var env = Pn(Pseg(Pseq([0 + 1e-4, 1, 0 + 1e-4], 1), dur*2, curve, inf)).asStream;
	var chance = Pwhite(0.0, 1.0, inf).asStream;

	Ppar([
		Pbindf(a, \instrument,
			Pfunc({|ev|
				var val;
				if((ev[\type] == 'monoNote').or(ev[\type] == 'monoSet'))
				{
					val = ev[\instrument];
					ev[\amp] = 1 - env.next;
				} {
					if(chance.next < env.next)
					{ val = \rest; }
					{ val = ev[\instrument]};
				};
				val;
			})
		),

		Pbindf(b, \instrument,
			Pfunc({|ev|
				var val;
				if((ev[\type] == 'monoNote').or(ev[\type] == 'monoSet'))
				{
					val = ev[\instrument];
					ev[\amp] = env.next;
				} {
					if(chance.next > env.next)
					{ val = \rest; }
					{ val = ev[\instrument]};
				};
				val;
			})
		)
	]);
}
)

~pchanceFade.(Pdef(\perc4), Pdef(\fb1Patt6), 4).play(t);

{SinOsc.ar()*01}.play;

Pdef(\a, Pbind(\dur, 0.125));
Pdef(\b, Pbind(\dur, 0.25, \freq, 72.midicps))



Pdef(\a, Pmono(\driftingSines_mono, \dur, 0.1, \amp, 1, \freq, 50, \pitchDev, 7.5, \numHarm, 7));
Pdef(\b, Pmono(\driftingSines_mono, \dur, 0.1, \amp, 1, \freq, 50, \pitchDev, 3, \numHarm, 15));

~pfadeInC.(Pdef(\a), 8).play;


(
~pfadeC.(Pdef(\a), Pdef(\b), 8, curve: \sine).play(t);
)


(
~pfadeD.(Pdef(\a), Pdef(\b), 16).play(t);
)