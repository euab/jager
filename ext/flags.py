import argparse
import logging

def parse_flags():
    parser = argparse.ArgumentParser(description="Flags to start the bot with.")
    parser.add_argument('--debug', help="Start the bot into debug mode.", action="store_true")
    parser.add_argument('--test-run', help="For CI testing", action="store_true")

    try:
        args = parser.parse_args()
    except Exception as e:
        # Logging has not been setup yet. So we can only print.
        print(e)
        exit(0)

    if args.debug:
        logging.basicConfig(level="DEBUG")
    else:
        logging.basicConfig(level="INFO")

    if args.test_run:
        logging.basicConfig(level="DEBUG")
        print("Running for CI")