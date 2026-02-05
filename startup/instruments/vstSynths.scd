(
    SynthDef(\vsti, {
        var sig =  VSTPlugin.ar(nil, 2);
        sig = sig * \amp.kr(1);
        Out.ar(\out.kr(0), sig);
    }).add;
)