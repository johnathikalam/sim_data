import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: const HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  Map<String, dynamic>? simDetails;
  bool isLoading = false;

  static const methodchannel = MethodChannel("Channel");

  // Fetch SIM details
  Future<void> getDetails() async {
    setState(() {
      isLoading = true;
    });

    try {
      final details = await methodchannel.invokeMethod("getSimDetails");

      if (details != null) {
        setState(() {
          simDetails = Map<String, dynamic>.from(details);
        });
      } else {
        setState(() {
          simDetails = {};
        });
        print("SIM details not available.");
      }
    } catch (e) {
      debugPrint(e.toString());
    } finally {
      setState(() {
        isLoading = false;
      });
    }
  }

  @override
  void initState() {
    super.initState();
    getDetails();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('SIM Details'),
        centerTitle: true,
      ),
      body: isLoading
          ? const Center(
        child: CircularProgressIndicator(),
      )
          : simDetails != null && simDetails!.isNotEmpty
          ? ListView.builder(
        itemCount: simDetails!.length ~/ 4, // Each SIM has 4 fields
        itemBuilder: (context, index) {
          return Container(
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(20),
              color: Colors.grey.shade200
            ),
            margin: const EdgeInsets.symmetric(
                horizontal: 10, vertical: 6),
            child: ListTile(
              title: Text(
                "SIM Slot ${index + 1}",
                style: const TextStyle(
                    fontWeight: FontWeight.bold, fontSize: 18),
              ),
              subtitle: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    "Carrier Name: ${simDetails!["carrierName$index"] ?? 'N/A'}",
                  ),
                  Text(
                    "Display Name: ${simDetails!["displayName$index"] ?? 'N/A'}",
                  ),
                  Text(
                    "Country: ${simDetails!["country$index"] ?? 'N/A'}",
                  ),
                ],
              ),
            ),
          );
        },
      )
          : const Center(
        child: Text(
          'No SIM details available.',
          style: TextStyle(fontSize: 18),
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: getDetails,
        backgroundColor: Colors.grey.shade200,
        foregroundColor: Colors.black,
        child: const Icon(Icons.refresh),
      ),
    );
  }
}
