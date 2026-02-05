(
~velvet = {|t, density, bias|

    var n = Dseries(0, 0, inf);

    var high = 1 - density;
    var low = density * 2;

    var nx1 = Diwhite(0, 1, inf);
    //bias
    var nx2 = (Dwhite(0, 1, inf) < (0.5 + (bias * 0.5))).if(1, -1);

    var out = Dswitch1([0, nx2], nx1 >= high);

	Demand.ar(t, 0, out);
};
)

b = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./NEW sample lib/Star Wars Jedi Knight Jedi Academy - All Sound Effects/player/fallsplat.wav");

(
Ndef(\fb2).clear;
Ndef(\fb2).ar(2);
)
(
Ndef(\fb2, {
	var sig, impulse, fb, modEnv1, del2, trigger;
	var time = \time.kr(0.1) - ControlRate.ir.reciprocal;
	var damp = (1 - \damp.kr(0.5));
	var res = (1 - \res.kr(0));

	fb = LocalIn.ar(2) * \feedback.kr(0.5);
	time = time*(1-fb);

	trigger = Impulse.ar(\impulse.kr(5000));
	impulse = ~velvet.(trigger, density: \density.kr(1), bias: \bias.kr(0));
	impulse = OnePole.ar(impulse, damp);
	sig = DelayC.ar(fb + impulse, 2, time);
	sig = sig + fb;
	sig = RLPF.ar(sig, freq: \filter.kr(440), rq: res);
	del2 = AllpassC.ar(sig, 2, time * 0.5);
	sig = SelectX.ar(\delay2.kr(0.8),[sig, del2]);
	sig = (sig * \dist.kr(36).dbamp).tanh;
	sig = SelectX.ar(\exciter.kr(0.7),[sig, impulse]);
	sig = OnePole.ar(sig, damp);
	sig = LeakDC.ar(in: sig, coef: 0.995);
	sig = sig.sanitize;
	sig = Convolution2.ar(sig, b.bufnum, impulse, 512, 0.5);
	// sig = sig + (NHHall.ar(sig, rt60: \rev.kr(0.1)) * -5.dbamp);

//	sig = BHPF.ar(sig, freq: 10);
	// sig = sig*0.99;

	LocalOut.ar(sig);
	//DetectSilence.ar(sig, doneAction: Done.freeSelf);
	// sig = sig * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(0.1), curve: -8), \gate.kr(1));
	// sig = sig * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(1), curve: -8), \gate.kr(1), doneAction: Done.freeSelf);
	sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig;
}).play;

Ndef(\fb2).addSpec(
	\impulse,[0, 44100,s.sampleRate.reciprocal],
	\damp,[0, 1],
	\time,[0.01, 2, \exp],
	\feedback,[0,1],
	\filter,[1, 20000, \exp],
	\res,[0,5],
	\dist,[0,64],
	\exciter,[0,1],
	\density,[0,1],
	\bias,[0,1]
).edit;
)