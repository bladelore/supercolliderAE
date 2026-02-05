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

~multiVelvet = { |triggers, density = 0.05, bias = 0|
    var nx2 = (Dwhite(0, 1, inf) < (0.5 + (bias * 0.5))) * 2 - 1;
    var out = Dswitch1([0, nx2], Dwhite(0, 1, inf) > (1 - density));

    triggers.collect{ |localTrig|
		  Demand.ar(localTrig, DC.ar(0), out);
    };
};

// ~multiVelvet = { |triggers, density = 0.05, bias = 0, stereo = 0|
//     var densityGate = Dwhite(0, 1, inf) > (1 - density);
//     var nx1 = (Dwhite(0, 1, inf) < (0.5 + (bias * 0.5))) * 2 - 1;
//     var n = Dswitch1([0, nx1], densityGate);
//     var nx2 = (Dwhite(0, 1, inf) < (0.5 + (bias * 0.5))) * 2 - 1;
//     var n2 = Dswitch1([0, nx2], densityGate);

//     n2 = Dswitch1([n, n2], (Dwhite(0, 1, inf) >= stereo));

//     triggers.collect{ |localTrig|
// 		  Demand.ar(localTrig, DC.ar(0), [n, n2]);
//     };
// };
)

