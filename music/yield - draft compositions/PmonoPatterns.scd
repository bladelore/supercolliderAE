
(
Pdef(\fmPad, Pmono(\fmPerc_mono) <> Pbind(
	\freq, 60,
	\dur, Pwhite(0.25, 1, inf) * 0.5,
	\dec, Pkey(\dur),
	\m1Ratio, 8,
	\m2Ratio, 0.25,
	//\m2Ratio, ~pmodenv.(Prand([4, 8, 12, 2], inf), Pkey(\dur), 1, \exponential),
	\m3Ratio, 1.5,
	//\m3Ratio, ~pmodenv.(Prand([2, 1, 0.5], inf), Pkey(\dec), 1, \sine),
	\carRatio, Pseq([1, 0.5, 2], inf).stutter(8),
	/*\mod1fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
	\mod2fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
	\mod3fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
	\carfb, ~pmodenv.(Pwhite(0, 0.6), Pkey(\dec)),*/
	\index1, ~pmodenv.(Pexprand(0.25, 2, inf), Pkey(\dec)),
	\index2, ~pmodenv.(Pexprand(0.25, 2, inf), Pkey(\dec)),
	\iscale, ~pmodenv.(Pexprand(0.01, 0.005, inf), Pkey(\dec)),
	\feedback, ~pmodenv.(Pwhite(1, 0, inf), Pkey(\dec)),
	\fbmod, ~pmodenv.(Pwhite(0, 1, inf), Pkey(\dec)),
	\spread,~pmodenv.(Pwhite(0, 30, inf), Pkey(\dec)),
	//\mix, ~pmodenv.(Pwhite(0.25, 0.75), Pkey(\dec)),
	\mix, 0,
	//\drive, 0,
	\drive, ~pmodenv.(Pwhite(0, 16), Pkey(\dec)),
	\gain, -12
	)
)
)

Pdef(\fmPad).play(t);


p.stop;

(

Pdef(\fb1Patt, Pmono(\fb1_mono) <> Pbind(
	\dur, Pwhite(0.125, 0.5, inf).stutter(4),
	//\legato, ~nearestDelta.(~pattern, Pkey(\bardelta), 0.25, 0).max(0.5),
	//\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.125, 1).linexp(0, 1, 0.1, 0.3),
	//\dec, ~pmodenv.(Pseq([1, 0.5, 0.25],inf), Pseq([2,1,0.5],inf)),
	//\dec, Pkey(\dur) * 1,
	\dec, Pkey(\dur) * 1,
	//\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 2) * 1,
	//\gain, ~nearestDelta.(~pattern, Pwhite(1, 0, inf), 0.125, 0).clip(0, 1).ampdb + dbamp(6),
//	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0, 0.5).clip(0, 1).ampdb,
	\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
	\time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 100),inf), Pkey(\dec)),
	//\time, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 0.01, 0.05, -8), Pkey(\dec), 1, Pseq([\step, \sine, -3],inf)),
	\res, ~pmodenv.(Pseq([1, 0],inf), Pseq([0.25, 0.25], inf)),
	\damp, ~pmodenv.(Pseq([0.1, 1],inf), Pkey(\dec)),
	//\damp, ~pmodenv.(Pwhite(1, 0.5, inf).lincurve(0, 1, 0.1, 1, -4), Pkey(\dec)),
	//\damp, 0.5,
	//\filter, 100,
	//\filter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0,1,2000,500,-4), Pkey(\dec)),
	//\res, 0.5,
	\filter, ~pmodenv.(Pseq([30, 100],inf), Pkey(\dec)),
	//\filter, ~pmodenv.(Pseq([60, 20000],inf), Pkey(\dec)),
	//\delay2, ~pmodenv.(Pwhite(1, 0, inf), Pkey(\dec)),
	\exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec), 1, Pseq([\sine],inf)),
	//\exciter, 0,
	//\impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pkey(\dec)),
	//\spont, ~pmodenv.(Pseq([60,30],inf), Pkey(\dec)),
	//\boost,  ~pmodenv.(Pseq([20000,200],inf), Pkey(\dec)),
	//\restore, ~pmodenv.(Pseq([1000, 20000],inf), Pkey(\dec)),
	\dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
	//\dist, 64,
	\rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
	\pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
	\gain, -6,
	)
).play(t, quant:[1,0])
);

(
Pdef(\grainSlicer_mono, Pmono(\grainSlicer_mono) <> Pbind(
	\dur, Pwhite(0.25, 1, inf) * 2,
	\dec, Pkey(\dur),
	\bufnum, ~src,
	\slice, Pseries(0, 1, inf).wrap(0, 32).stutter(1),
	\posRate, ~pmodenv.(Pwhite(0.05, 0.5, inf), Pkey(\dec)),
	\polarityMod, ~pmodenv.(Pwhite(0, 1, inf), Pkey(\dec), 1, \sine),
	\overlap, ~pmodenv.(Pwhite(40, 100, inf).lincurve(0, 1, 1, 40, -4), Pkey(\dec)),
	\panMod, Pwhite(1, 1, inf),
	\trigRate, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 200, 2000, 2, -8), Pkey(\dec)),
	\pan_master, ~pmodenv.(Pseq([-1, 0, 1],inf), Pkey(\dec), 1, \sine),
	\rate, 1,
	\gain, 0,
	)).play(t)
);

(
Pdef(\fmPercPatt, Pbind(
	\instrument, \fmPerc,
	\freq, 60,
	\dur, Pwhite(0.5, 0.125, inf),
	//\dur, 0.5,
	\dec, Pkey(\dur) * 0.5,
	\atk, ~pmodenv.(Pwhite(0.01, 1, inf), Pkey(\dec)),
	//\rel, ~pmodenv.(Pwhite(0.5, 0.125, inf), Pkey(\dec)),
	\rel, 0.5,
	\feedback, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
	\fbmod, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
	\carRatio, 0.5,
	\m1Ratio, 1,
	\m2Ratio, 7,
	\m3Ratio, 9,
	\i1atk, ~pmodenv.(Pwhite(0.5, 0.1), Pkey(\dec)),
	\i2atk, ~pmodenv.(Pwhite(0.8, 0.01), Pkey(\dec)),
	\i1rel, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
	\i2rel, ~pmodenv.(Pwhite(0, 0.5), Pkey(\dec)),
	//\m3Ratio, ~pmodenv.(Prand([2, 1, 0.5], inf), Pkey(\dec), 1, \sine),
	//\carRatio, Pseq([1, 0.5, 2], inf).stutter(8),
/*	\mod1fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
	\mod2fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
	\mod3fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
	\carfb, ~pmodenv.(Pwhite(0, 0.6), Pkey(\dec)),*/
	\index1, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
	\index2, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
	\iscale, ~pmodenv.(Pexprand(1, 0.1, inf), Pkey(\dec)),
	\spread,~pmodenv.(Pwhite(10, 30, inf), Pkey(\dec)),
	\mix, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
	//\mix, 1,
	//\drive, 0,
	\drive, ~pmodenv.(Pwhite(0, 16), Pkey(\dec)),
	\gain, -12,
)
	).play(t, quant:[1,0])
);

Pdef.clear;
Pdef(\fmPercPatt).play(t, quant:[1,0]);

(
Pdef(\fb1Patt, Pmono(\fb1_mono) <> Pbind(
	\dur, Pwhite(0.125, 0.5, inf).stutter(4),
	//\legato, ~nearestDelta.(~pattern, Pkey(\bardelta), 0.25, 0).max(0.5),
	//\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.125, 1).linexp(0, 1, 0.1, 0.3),
	//\dec, ~pmodenv.(Pseq([1, 0.5, 0.25],inf), Pseq([2,1,0.5],inf)),
	//\dec, Pkey(\dur) * 1,
	\dec, Pkey(\dur) * 1,
	//\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 2) * 1,
	//\gain, ~nearestDelta.(~pattern, Pwhite(1, 0, inf), 0.125, 0).clip(0, 1).ampdb + dbamp(6),
//	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0, 0.5).clip(0, 1).ampdb,
	\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
	\time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 100),inf), Pkey(\dec)),
	//\time, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 0.01, 0.05, -8), Pkey(\dec), 1, Pseq([\step, \sine, -3],inf)),
	\res, ~pmodenv.(Pseq([1, 0],inf), Pseq([0.25, 0.25], inf)),
	\damp, ~pmodenv.(Pseq([0.1, 1],inf), Pkey(\dec)),
	//\damp, ~pmodenv.(Pwhite(1, 0.5, inf).lincurve(0, 1, 0.1, 1, -4), Pkey(\dec)),
	//\damp, 0.5,
	//\filter, 100,
	//\filter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0,1,2000,500,-4), Pkey(\dec)),
	//\res, 0.5,
	\filter, ~pmodenv.(Pseq([30, 100],inf), Pkey(\dec)),
	//\filter, ~pmodenv.(Pseq([60, 20000],inf), Pkey(\dec)),
	//\delay2, ~pmodenv.(Pwhite(1, 0, inf), Pkey(\dec)),
	\exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec), 1, Pseq([\sine],inf)),
	//\exciter, 0,
	//\impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pkey(\dec)),
	//\spont, ~pmodenv.(Pseq([60,30],inf), Pkey(\dec)),
	//\boost,  ~pmodenv.(Pseq([20000,200],inf), Pkey(\dec)),
	//\restore, ~pmodenv.(Pseq([1000, 20000],inf), Pkey(\dec)),
	\dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
	//\dist, 64,
	\rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
	\pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
	\gain, -6,
	)
).play(t, quant:[1,0])
);

(
Pdef(\fb1Patt, Pmono(\fb1_mono) <> Pbind(
	\dur, Pwhite(0.125, 0.5, inf),
	//\legato, ~nearestDelta.(~pattern, Pkey(\bardelta), 0.25, 0).max(0.5),
	//\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.125, 1).linexp(0, 1, 0.1, 0.3),
	//\dec, ~pmodenv.(Pseq([1, 0.5, 0.25],inf), Pseq([2,1,0.5],inf)),
	//\dec, Pkey(\dur) * 1,
	\dec, Pkey(\dur) * 1,
	//\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 2) * 1,
	//\gain, ~nearestDelta.(~pattern, Pwhite(1, 0, inf), 0.125, 0).clip(0, 1).ampdb + dbamp(6),
//	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0, 0.5).clip(0, 1).ampdb,
	\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pkey(\dec)),
	\time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 1),inf), Pkey(\dec)),
	//\time, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 0.01, 0.05, -8), Pkey(\dec), 1, Pseq([\step, \sine, -3],inf)),
	//\res, ~pmodenv.(Pseq([1, 0],inf), Pseq([0.25, 0.25], inf)),
	\damp, ~pmodenv.(Pseq([0.5, 1],inf), Pkey(\dec)),
	//\damp, ~pmodenv.(Pwhite(1, 0.5, inf).lincurve(0, 1, 0.1, 1, -4), Pkey(\dec)),
	//\damp, 0.5,
	//\filter, 100,
	//\filter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0,1,2000,500,-4), Pkey(\dec)),
	//\res, 0.5,
	//\filter, ~pmodenv.(Pseq([30, 100],inf), Pkey(\dec)),
	\filter, ~pmodenv.(Pwhite(5000, 20000,inf), Pkey(\dec)),
	\delay2, ~pmodenv.(Pwhite(1, 0, inf), Pkey(\dec)),
	//\exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec), 1, Pseq([\sine],inf)),
	\exciter, 0,
	//\impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pkey(\dec)),
	//\spont, ~pmodenv.(Pseq([60,30],inf), Pkey(\dec)),
	//\boost,  ~pmodenv.(Pseq([20000,200],inf), Pkey(\dec)),
	//\restore, ~pmodenv.(Pseq([1000, 20000],inf), Pkey(\dec)),
	\dist,  ~pmodenv.(Pseq([16, 32] * 2,inf), Pkey(\dec)),
	//\dist, 64,
	\rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
	\pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
	\gain, -6,
	)
).play(t, quant:[1,0])
);