(
SynthDef(\tape, {
	var xin, x, y, y2, gainDiff, sig, postgain;
	//xin = In.ar(in, 2);
	xin = SinOsc.ar(440) * 0.01 ! 2;
	x = (xin * \pregain.kr(30).dbamp) + \dc_offset.kr(0);
	y = (2 / (1 + exp(\sigmoid.kr(1).neg * 100 * x))) - 1;
	y2 = (y - 0.761594) / -98;
	postgain = \postgain.kr(0).dbamp;
	gainDiff = xin-(y2*postgain);
	y = Select.ar(\autogain.kr(0), [y * postgain, y * gainDiff]);
	sig = SelectX.ar(\drywet.kr(1), [xin, y]);
	Out.ar(\out.kr(0), sig);
}).add;
)

Synth(\tape, [drywet: 0.5, pregain: 30, sigmoid: 1, autogain: 0, ]);