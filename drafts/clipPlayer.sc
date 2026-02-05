(
~clipPlayer = {|key, clipPointer, songArr|
    k.elAt(\tr, [\rew, \fwd, \stop, \play]).do{|sl, buttonIdx|
    var selectedFunc, prevFunc;

    sl.action = {|el|
        if((buttonIdx == 0) || (buttonIdx == 1)){
            if(el.value == 1){
                if(buttonIdx == 0){ clipPointer = (clipPointer - 1).max(0)};
                if(buttonIdx == 1){ clipPointer = (clipPointer + 1).clip(0, songArr.size-1)};
                clipPointer.postln;
                selectedFunc = songArr.at(clipPointer);
                Tdef(key, selectedFunc);
            };
        };
        
        if(buttonIdx == 2){ 
            if(el.value == 1){
                Tdef(key).stop(t);
                Pdef.all.keys.do({|key| if(Pdef(key).isActive){Pdef(key).stop(t)}});
                "stopping all patterns/ndef".postln;
            };
        };
        if(buttonIdx == 3){
            if(el.value == 1){
                Tdef(key).play(t);
                ("playing clip: "++clipPointer).postln;
            };
        };

    }
};
};
)

Tdef(\hi).clear;
Tdef(\hi);
~clipPlayer.(\hi, ~clipPointer, ~songArr);

(
a = {
     var ipf, specGrains, additive;
    var specBuff = ~specBuff_A;
    var sample = ~sewer;

    ~bufA.zero;
    ~bufB.zero;
    ~specBuf.do{|item| item.zero};

    Pdef(\bell,
        Pmono(\fftStretch_magFilter_mono,
            \dur, 0.1,
            \amp, ~pmodenv.(Pseq([0.1, 1], inf), 4, 1, \exp),
            \gain, -12,
            \buf, specBuff.at(\file),
            \analysis, [specBuff.at(\analysis)],
            \fftSize, specBuff.at(\fftSize),
            \rate, 0.5,
            \thresh, 10,
            \remove, 0,

            \thresh, ~pmodenv.(Pseq([10, 100], inf), 8, 1, \sin),
            \remove, ~pmodenv.(Pseq([1, 10], inf), 6, 1, \sin),
            \pos, ~pmodenv.(Pseq([0.4, 0.45], inf), Pseq([8,1],inf), 1, \exp),
            \len, 0.01,
            \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
            \pitchRatio, 0.75 * 0.5,
            \out, [~bus3]
        )
    ).play(t);
};

b = {
    Pdef(\bo, Pbind(\dur, 0.5, \octave, 7)).play(t);
};

~songArr = [a, b]
)


(
a = {
    Pdef(\bo, Pbind(\dur, 1)).play(t);
};

b = {
    Pdef(\bo, Pbind(\dur, 0.5, \octave, 7)).play(t);
};

~songArr = [a, b]
)

////

~advance = Condition(true)

~advance.test = true;

k.gui

(
~advance = Condition.new;
k.elAt(\tr, [\fwd]).do{|sl, buttonIdx| 
    sl.action = {|el|
        if (el.value == 1) {
            ~advance.test = true;
            ~advance.signal;
            ~advance.test = false;
            "next".postln;
        };
    };
};

~a = Pbind(\degree, -4);
~b = Pbind(\degree, -7, \dur, 3/7);

~routine = Routine({
	Pdef(\player).play;
	
	////////////////////////////////////////////////////////////////////////
	\a.postln;
	Pdef(\player).source = Ppar([
		~a, ~b
	]);
	
	~advance.wait; /////////////////////////////////////////////////////////
	
    \b.postln;
	Pdef(\player).source = (
		Pfunc({ |e| e[\degree] = e[\degree] - 3 }) 
		<> Ppar([
			~a, ~b
		])
	);
	
	~advance.wait; /////////////////////////////////////////////////////////
	
    \c.postln;
	Pdef(\player).source = (
		Ppar([
			Pset(\dur, 3/5, ~a), 
			Pset(\dur, 4/11, ~b)
		])
	);

}).play;
)

c = Condition(false); fork { 0.5.wait; "started ...".postln; c.wait;  "... and finished.".postln };
c.test = true;
c.signal;

c = Condition.new; fork { 0.5.wait; "started ...".postln; c.hang;  "... and finished.".postln };
c.unhang;